package org.chagolchana.noconnect.api.autodelete.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when one or more messages
 * in the private conversation with a contact have been deleted.
 */
@Immutable
@NotNullByDefault
public class ConversationMessagesDeletedEvent extends Event {

	private final ContactId contactId;
	private final Collection<MessageId> messageIds;

	public ConversationMessagesDeletedEvent(ContactId contactId,
			Collection<MessageId> messageIds) {
		this.contactId = contactId;
		this.messageIds = messageIds;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public Collection<MessageId> getMessageIds() {
		return messageIds;
	}
}
