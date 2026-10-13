package org.chagolchana.chagol.api.contact.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a contact is added.
 */
@Immutable
@NotNullByDefault
public class ContactAddedEvent extends Event {

	private final ContactId contactId;
	private final boolean verified;

	public ContactAddedEvent(ContactId contactId, boolean verified) {
		this.contactId = contactId;
		this.verified = verified;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public boolean isVerified() {
		return verified;
	}
}
