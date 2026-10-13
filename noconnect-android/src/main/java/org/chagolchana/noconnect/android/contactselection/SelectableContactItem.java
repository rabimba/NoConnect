package org.chagolchana.noconnect.android.contactselection;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.noconnect.api.sharing.SharingManager.SharingStatus;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.NotThreadSafe;

import static org.chagolchana.noconnect.api.sharing.SharingManager.SharingStatus.SHAREABLE;

@NotThreadSafe
@NotNullByDefault
public class SelectableContactItem extends BaseSelectableContactItem {

	private final SharingStatus sharingStatus;

	public SelectableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected, SharingStatus sharingStatus) {
		super(contact, authorInfo, selected);
		this.sharingStatus = sharingStatus;
	}

	public SharingStatus getSharingStatus() {
		return sharingStatus;
	}

	@Override
	public boolean isDisabled() {
		return sharingStatus != SHAREABLE;
	}

}
