package org.chagolchana.noconnect.api.autodelete.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class AutoDeleteTimerMirroredEvent extends Event {

	private final ContactId contactId;
	private final long newTimer;

	public AutoDeleteTimerMirroredEvent(ContactId contactId, long newTimer) {
		this.contactId = contactId;
		this.newTimer = newTimer;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public long getNewTimer() {
		return newTimer;
	}
}
