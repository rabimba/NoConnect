package org.chagolchana.noconnect.android.contactselection;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.noconnect.android.contact.ContactItem;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
public abstract class BaseSelectableContactItem extends ContactItem {

	private boolean selected;

	public BaseSelectableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected) {
		super(contact, authorInfo);
		this.selected = selected;
	}

	boolean isSelected() {
		return selected;
	}

	void toggleSelected() {
		selected = !selected;
	}

	public abstract boolean isDisabled();

}
