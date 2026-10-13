package org.chagolchana.noconnect.api.introduction.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.noconnect.api.conversation.event.ConversationMessageReceivedEvent;
import org.chagolchana.noconnect.api.introduction.IntroductionResponse;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionResponseReceivedEvent extends
		ConversationMessageReceivedEvent<IntroductionResponse> {

	public IntroductionResponseReceivedEvent(
			IntroductionResponse introductionResponse, ContactId contactId) {
		super(introductionResponse, contactId);
	}

}
