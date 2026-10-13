package org.chagolchana.noconnect.android.privategroup.reveal;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.noconnect.android.contactselection.BaseSelectableContactItem;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.noconnect.api.privategroup.Visibility;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.NotThreadSafe;

import static org.chagolchana.noconnect.api.privategroup.Visibility.INVISIBLE;

@NotThreadSafe
@NotNullByDefault
class RevealableContactItem extends BaseSelectableContactItem {

	private final Visibility visibility;

	RevealableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected, Visibility visibility) {
		super(contact, authorInfo, selected);
		this.visibility = visibility;
	}

	Visibility getVisibility() {
		return visibility;
	}

	@Override
	public boolean isDisabled() {
		return visibility != INVISIBLE;
	}
}
