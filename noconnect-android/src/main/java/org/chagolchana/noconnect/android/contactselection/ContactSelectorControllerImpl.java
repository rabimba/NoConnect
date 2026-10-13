package org.chagolchana.noconnect.android.contactselection;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.android.controller.DbControllerImpl;
import org.chagolchana.noconnect.android.controller.handler.ResultExceptionHandler;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.noconnect.api.identity.AuthorManager;
import org.chagolchana.noconnect.api.sharing.SharingManager.SharingStatus;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import static java.util.logging.Level.WARNING;
import static org.chagolchana.chagol.util.LogUtils.logException;

@Immutable
@NotNullByDefault
public abstract class ContactSelectorControllerImpl
		extends DbControllerImpl
		implements ContactSelectorController<SelectableContactItem> {

	private static final Logger LOG =
			Logger.getLogger(ContactSelectorControllerImpl.class.getName());

	private final ContactManager contactManager;
	private final AuthorManager authorManager;

	public ContactSelectorControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			AuthorManager authorManager) {
		super(dbExecutor, lifecycleManager);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
	}

	@Override
	public void loadContacts(GroupId g, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<SelectableContactItem>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				Collection<SelectableContactItem> contacts = new ArrayList<>();
				for (Contact c : contactManager.getContacts()) {
					AuthorInfo authorInfo = authorManager.getAuthorInfo(c);
					// was this contact already selected?
					boolean selected = selection.contains(c.getId());
					contacts.add(new SelectableContactItem(c, authorInfo,
							selected, getSharingStatus(g, c)));
				}
				handler.onResult(contacts);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}

	@DatabaseExecutor
	protected abstract SharingStatus getSharingStatus(GroupId g, Contact c)
			throws DbException;

}
