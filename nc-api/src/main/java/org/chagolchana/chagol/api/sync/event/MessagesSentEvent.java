package org.chagolchana.chagol.api.sync.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when messages are sent to a contact.
 */
@Immutable
@NotNullByDefault
public class MessagesSentEvent extends Event {

	private final ContactId contactId;
	private final Collection<MessageId> messageIds;
	private final long totalLength;

	public MessagesSentEvent(ContactId contactId,
			Collection<MessageId> messageIds, long totalLength) {
		this.contactId = contactId;
		this.messageIds = messageIds;
		this.totalLength = totalLength;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public Collection<MessageId> getMessageIds() {
		return messageIds;
	}

	public long getTotalLength() {
		return totalLength;
	}
}
