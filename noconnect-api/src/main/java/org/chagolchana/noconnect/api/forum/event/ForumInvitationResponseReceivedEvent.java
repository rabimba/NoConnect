package org.chagolchana.noconnect.api.forum.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.noconnect.api.conversation.event.ConversationMessageReceivedEvent;
import org.chagolchana.noconnect.api.forum.ForumInvitationResponse;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumInvitationResponseReceivedEvent extends
		ConversationMessageReceivedEvent<ForumInvitationResponse> {

	public ForumInvitationResponseReceivedEvent(
			ForumInvitationResponse response, ContactId contactId) {
		super(response, contactId);
	}

}
