package org.chagolchana.noconnect.android.contactselection;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import androidx.annotation.UiThread;

@NotNullByDefault
public interface ContactSelectorListener {

	@UiThread
	void contactsSelected(Collection<ContactId> contacts);

}
