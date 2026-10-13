package org.chagolchana.noconnect.api.blog.event;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.noconnect.api.blog.Blog;
import org.chagolchana.noconnect.api.conversation.ConversationRequest;
import org.chagolchana.noconnect.api.conversation.event.ConversationMessageReceivedEvent;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class BlogInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<ConversationRequest<Blog>> {

	public BlogInvitationRequestReceivedEvent(ConversationRequest<Blog> request,
			ContactId contactId) {
		super(request, contactId);
	}

}
