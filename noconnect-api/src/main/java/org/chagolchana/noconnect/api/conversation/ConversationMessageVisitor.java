package org.chagolchana.noconnect.api.conversation;

import org.chagolchana.noconnect.api.blog.BlogInvitationRequest;
import org.chagolchana.noconnect.api.blog.BlogInvitationResponse;
import org.chagolchana.noconnect.api.forum.ForumInvitationRequest;
import org.chagolchana.noconnect.api.forum.ForumInvitationResponse;
import org.chagolchana.noconnect.api.introduction.IntroductionRequest;
import org.chagolchana.noconnect.api.introduction.IntroductionResponse;
import org.chagolchana.noconnect.api.messaging.PrivateMessageHeader;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationRequest;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationResponse;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ConversationMessageVisitor<T> {

	T visitPrivateMessageHeader(PrivateMessageHeader h);

	T visitBlogInvitationRequest(BlogInvitationRequest r);

	T visitBlogInvitationResponse(BlogInvitationResponse r);

	T visitForumInvitationRequest(ForumInvitationRequest r);

	T visitForumInvitationResponse(ForumInvitationResponse r);

	T visitGroupInvitationRequest(GroupInvitationRequest r);

	T visitGroupInvitationResponse(GroupInvitationResponse r);

	T visitIntroductionRequest(IntroductionRequest r);

	T visitIntroductionResponse(IntroductionResponse r);
}
