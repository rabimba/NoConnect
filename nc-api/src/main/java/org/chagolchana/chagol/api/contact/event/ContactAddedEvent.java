package org.chagolchana.chagol.api.contact.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a contact is added.
 */
@Immutable
@NotNullByDefault
public class ContactAddedEvent extends Event {

	private final ContactId contactId;
	private final boolean active;

	public ContactAddedEvent(ContactId contactId, boolean active) {
		this.contactId = contactId;
		this.active = active;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public boolean isActive() {
		return active;
	}
}
