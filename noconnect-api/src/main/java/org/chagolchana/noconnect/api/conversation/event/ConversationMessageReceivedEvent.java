package org.chagolchana.noconnect.api.conversation.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.noconnect.api.conversation.ConversationMessageHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new conversation message is received.
 */
@Immutable
@NotNullByDefault
public abstract class ConversationMessageReceivedEvent<H extends ConversationMessageHeader>
		extends Event {

	private final H messageHeader;
	private final ContactId contactId;

	public ConversationMessageReceivedEvent(H messageHeader,
			ContactId contactId) {
		this.messageHeader = messageHeader;
		this.contactId = contactId;
	}

	public H getMessageHeader() {
		return messageHeader;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
