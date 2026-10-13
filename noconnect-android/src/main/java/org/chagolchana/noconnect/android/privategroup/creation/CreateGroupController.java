package org.chagolchana.noconnect.android.privategroup.creation;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.android.contactselection.ContactSelectorController;
import org.chagolchana.noconnect.android.contactselection.SelectableContactItem;
import org.chagolchana.noconnect.android.controller.handler.ResultExceptionHandler;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import androidx.annotation.Nullable;

@NotNullByDefault
public interface CreateGroupController
		extends ContactSelectorController<SelectableContactItem> {

	void createGroup(String name,
			ResultExceptionHandler<GroupId, DbException> result);

	void sendInvitation(GroupId g, Collection<ContactId> contacts,
			@Nullable String text,
			ResultExceptionHandler<Void, DbException> result);

}
