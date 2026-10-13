package org.chagolchana.chagol.api.mailbox.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.mailbox.MailboxUpdate;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when {@link MailboxUpdate} are received
 * from a contact.
 */
@Immutable
@NotNullByDefault
public class RemoteMailboxUpdateEvent extends Event {

	private final ContactId contactId;
	private final MailboxUpdate mailboxUpdate;

	public RemoteMailboxUpdateEvent(ContactId contactId,
			MailboxUpdate mailboxUpdate) {
		this.contactId = contactId;
		this.mailboxUpdate = mailboxUpdate;
	}

	public ContactId getContact() {
		return contactId;
	}

	public MailboxUpdate getMailboxUpdate() {
		return mailboxUpdate;
	}
}
