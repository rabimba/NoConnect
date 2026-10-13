package org.chagolchana.chagol.api.mailbox.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.mailbox.MailboxUpdate;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a mailbox is unpaired.
 */
@Immutable
@NotNullByDefault
public class MailboxUnpairedEvent extends Event {

	private final Map<ContactId, MailboxUpdate> localUpdates;

	public MailboxUnpairedEvent(Map<ContactId, MailboxUpdate> localUpdates) {
		this.localUpdates = localUpdates;
	}

	public Map<ContactId, MailboxUpdate> getLocalUpdates() {
		return localUpdates;
	}
}
