package org.chagolchana.chagol.api.mailbox.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.mailbox.MailboxProperties;
import org.chagolchana.chagol.api.mailbox.MailboxUpdateWithMailbox;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a mailbox is paired.
 */
@Immutable
@NotNullByDefault
public class MailboxPairedEvent extends Event {

	private final MailboxProperties properties;
	private final Map<ContactId, MailboxUpdateWithMailbox> localUpdates;

	public MailboxPairedEvent(MailboxProperties properties,
			Map<ContactId, MailboxUpdateWithMailbox> localUpdates) {
		this.properties = properties;
		this.localUpdates = localUpdates;
	}

	public MailboxProperties getProperties() {
		return properties;
	}

	public Map<ContactId, MailboxUpdateWithMailbox> getLocalUpdates() {
		return localUpdates;
	}
}
