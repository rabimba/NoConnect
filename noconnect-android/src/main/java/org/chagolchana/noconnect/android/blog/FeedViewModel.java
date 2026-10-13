package org.chagolchana.noconnect.android.blog;

import android.app.Application;

import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.event.GroupRemovedEvent;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.android.viewmodel.LiveResult;
import org.chagolchana.noconnect.api.android.AndroidNotificationManager;
import org.chagolchana.noconnect.api.blog.Blog;
import org.chagolchana.noconnect.api.blog.BlogManager;
import org.chagolchana.noconnect.api.blog.event.BlogPostAddedEvent;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.ArrayList;
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
import static org.chagolchana.noconnect.api.blog.BlogManager.CLIENT_ID;

@NotNullByDefault
class FeedViewModel extends BaseViewModel {

	private static final Logger LOG = getLogger(FeedViewModel.class.getName());

	private final MutableLiveData<Blog> personalBlog = new MutableLiveData<>();

	@Inject
	FeedViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			EventBus eventBus,
			IdentityManager identityManager,
			AndroidNotificationManager notificationManager,
			BlogManager blogManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				eventBus, identityManager, notificationManager, blogManager);
		loadPersonalBlog();
		loadAllBlogPosts();
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof BlogPostAddedEvent) {
			BlogPostAddedEvent b = (BlogPostAddedEvent) e;
			LOG.info("Blog post added");
			onBlogPostAdded(b.getHeader(), b.isLocal());
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getClientId().equals(CLIENT_ID)) {
				LOG.info("Blog removed");
				onBlogRemoved(g.getGroup().getId());
			}
		}
	}

	void blockAndClearAllBlogPostNotifications() {
		notificationManager.blockAllBlogPostNotifications();
		notificationManager.clearAllBlogPostNotifications();
	}

	void unblockAllBlogPostNotifications() {
		notificationManager.unblockAllBlogPostNotifications();
	}

	private void loadPersonalBlog() {
		runOnDbThread(() -> {
			try {
				long start = now();
				Author a = identityManager.getLocalAuthor();
				Blog b = blogManager.getPersonalBlog(a);
				logDuration(LOG, "Loading personal blog", start);
				personalBlog.postValue(b);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Blog> getPersonalBlog() {
		return personalBlog;
	}

	private void loadAllBlogPosts() {
		loadFromDb(this::loadAllBlogPosts, blogPosts::setValue);
	}

	@DatabaseExecutor
	private ListUpdate loadAllBlogPosts(Transaction txn)
			throws DbException {
		long start = now();
		List<BlogPostItem> posts = new ArrayList<>();
		for (GroupId g : blogManager.getBlogIds(txn)) {
			posts.addAll(loadBlogPosts(txn, g));
		}
		Collections.sort(posts);
		logDuration(LOG, "Loading all posts", start);
		return new ListUpdate(null, posts);
	}

	@UiThread
	private void onBlogRemoved(GroupId g) {
		List<BlogPostItem> items = removeListItems(getBlogPostItems(), item ->
				item.getGroupId().equals(g)
		);
		if (items != null) {
			blogPosts.setValue(new LiveResult<>(new ListUpdate(null, items)));
		}
	}

}
