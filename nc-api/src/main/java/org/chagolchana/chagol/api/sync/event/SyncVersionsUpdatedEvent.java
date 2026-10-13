package org.chagolchana.chagol.api.sync.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when the versions of the sync protocol supported
 * by a contact are updated.
 */
@Immutable
@NotNullByDefault
public class SyncVersionsUpdatedEvent extends Event {

	private final ContactId contactId;
	private final List<Byte> supported;

	public SyncVersionsUpdatedEvent(ContactId contactId, List<Byte> supported) {
		this.contactId = contactId;
		this.supported = supported;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public List<Byte> getSupportedVersions() {
		return supported;
	}
}
