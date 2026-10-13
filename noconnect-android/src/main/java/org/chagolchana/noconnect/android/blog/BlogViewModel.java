package org.chagolchana.noconnect.android.blog;

import android.app.Application;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.identity.LocalAuthor;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.event.GroupRemovedEvent;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.android.sharing.SharingController;
import org.chagolchana.noconnect.android.sharing.SharingController.SharingInfo;
import org.chagolchana.noconnect.api.android.AndroidNotificationManager;
import org.chagolchana.noconnect.api.blog.Blog;
import org.chagolchana.noconnect.api.blog.BlogInvitationResponse;
import org.chagolchana.noconnect.api.blog.BlogManager;
import org.chagolchana.noconnect.api.blog.BlogSharingManager;
import org.chagolchana.noconnect.api.blog.event.BlogInvitationResponseReceivedEvent;
import org.chagolchana.noconnect.api.blog.event.BlogPostAddedEvent;
import org.chagolchana.noconnect.api.sharing.event.ContactLeftShareableEvent;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.util.LogUtils.logDuration;
import static org.chagolchana.chagol.util.LogUtils.now;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
class BlogViewModel extends BaseViewModel {

	private static final Logger LOG = getLogger(BlogViewModel.class.getName());

	private final BlogSharingManager blogSharingManager;
	private final SharingController sharingController;

	private volatile GroupId groupId;

	private final MutableLiveData<BlogItem> blog = new MutableLiveData<>();
	private final MutableLiveData<Boolean> blogRemoved =
			new MutableLiveData<>();

	@Inject
	BlogViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			EventBus eventBus,
			IdentityManager identityManager,
			AndroidNotificationManager notificationManager,
			BlogManager blogManager,
			BlogSharingManager blogSharingManager,
			SharingController sharingController) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				eventBus, identityManager, notificationManager, blogManager);
		this.blogSharingManager = blogSharingManager;
		this.sharingController = sharingController;
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof BlogPostAddedEvent) {
			BlogPostAddedEvent b = (BlogPostAddedEvent) e;
			if (b.getGroupId().equals(groupId)) {
				LOG.info("Blog post added");
				onBlogPostAdded(b.getHeader(), b.isLocal());
			}
		} else if (e instanceof BlogInvitationResponseReceivedEvent) {
			BlogInvitationResponseReceivedEvent b =
					(BlogInvitationResponseReceivedEvent) e;
			BlogInvitationResponse r = b.getMessageHeader();
			if (r.getShareableId().equals(groupId) && r.wasAccepted()) {
				LOG.info("Blog invitation accepted");
				sharingController.add(b.getContactId());
			}
		} else if (e instanceof ContactLeftShareableEvent) {
			ContactLeftShareableEvent s = (ContactLeftShareableEvent) e;
			if (s.getGroupId().equals(groupId)) {
				LOG.info("Blog left by contact");
				sharingController.remove(s.getContactId());
			}
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getId().equals(groupId)) {
				LOG.info("Blog removed");
				blogRemoved.setValue(true);
			}
		}
	}

	/**
	 * Set this before calling any other methods.
	 */
	@UiThread
	public void setGroupId(GroupId groupId, boolean loadAllPosts) {
		if (this.groupId == groupId) return; // configuration change
		this.groupId = groupId;
		loadBlog(groupId);
		if (loadAllPosts) loadBlogPosts(groupId);
		loadSharingContacts(groupId);
	}

	private void loadBlog(GroupId groupId) {
		runOnDbThread(() -> {
			try {
				long start = now();
				LocalAuthor a = identityManager.getLocalAuthor();
				Blog b = blogManager.getBlog(groupId);
				boolean ours = a.getId().equals(b.getAuthor().getId());
				boolean removable = blogManager.canBeRemoved(b);
				blog.postValue(new BlogItem(b, ours, removable));
				logDuration(LOG, "Loading blog", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	void blockAndClearNotifications() {
		notificationManager.blockNotification(groupId);
		notificationManager.clearBlogPostNotification(groupId);
	}

	void unblockNotifications() {
		notificationManager.unblockNotification(groupId);
	}

	private void loadBlogPosts(GroupId groupId) {
		loadFromDb(txn -> {
			List<BlogPostItem> posts = loadBlogPosts(txn, groupId);
			Collections.sort(posts);
			return new ListUpdate(null, posts);
		}, blogPosts::setValue);
	}

	private void loadSharingContacts(GroupId groupId) {
		runOnDbThread(true, txn -> {
			Collection<Contact> contacts =
					blogSharingManager.getSharedWith(txn, groupId);
			txn.attach(() -> onSharingContactsLoaded(contacts));
		}, this::handleException);
	}

	@UiThread
	private void onSharingContactsLoaded(Collection<Contact> contacts) {
		Collection<ContactId> contactIds = new ArrayList<>(contacts.size());
		for (Contact c : contacts) contactIds.add(c.getId());
		sharingController.addAll(contactIds);
	}

	void deleteBlog() {
		runOnDbThread(() -> {
			try {
				long start = now();
				Blog b = blogManager.getBlog(groupId);
				blogManager.removeBlog(b);
				logDuration(LOG, "Removing blog", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<BlogItem> getBlog() {
		return blog;
	}

	LiveData<Boolean> getBlogRemoved() {
		return blogRemoved;
	}

	LiveData<SharingInfo> getSharingInfo() {
		return sharingController.getSharingInfo();
	}
}
