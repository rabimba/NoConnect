package org.chagolchana.noconnect.api.introduction.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.noconnect.api.conversation.event.ConversationMessageReceivedEvent;
import org.chagolchana.noconnect.api.introduction.IntroductionRequest;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionRequestReceivedEvent
		extends ConversationMessageReceivedEvent<IntroductionRequest> {

	public IntroductionRequestReceivedEvent(
			IntroductionRequest introductionRequest, ContactId contactId) {
		super(introductionRequest, contactId);
	}

}
