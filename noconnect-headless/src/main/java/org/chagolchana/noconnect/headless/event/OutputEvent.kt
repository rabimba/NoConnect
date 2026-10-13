package org.chagolchana.noconnect.headless.event

import org.chagolchana.noconnect.api.blog.BlogInvitationRequest
import org.chagolchana.noconnect.api.blog.BlogInvitationResponse
import org.chagolchana.noconnect.api.conversation.event.ConversationMessageReceivedEvent
import org.chagolchana.noconnect.api.forum.ForumInvitationRequest
import org.chagolchana.noconnect.api.forum.ForumInvitationResponse
import org.chagolchana.noconnect.api.introduction.IntroductionRequest
import org.chagolchana.noconnect.api.introduction.IntroductionResponse
import org.chagolchana.noconnect.api.messaging.PrivateMessageHeader
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationRequest
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationResponse
import org.chagolchana.noconnect.headless.json.JsonDict
import org.chagolchana.noconnect.headless.messaging.output
import javax.annotation.concurrent.Immutable

@Immutable
@Suppress("unused")
internal class OutputEvent(val name: String, val data: JsonDict) {
    val type = "event"
}

internal fun ConversationMessageReceivedEvent<*>.output(text: String?): JsonDict {
    check(messageHeader is PrivateMessageHeader)
    return (messageHeader as PrivateMessageHeader).output(contactId, text)
}

internal fun ConversationMessageReceivedEvent<*>.output() = when (messageHeader) {
    // requests
    is ForumInvitationRequest -> (messageHeader as ForumInvitationRequest).output(contactId)
    is BlogInvitationRequest -> (messageHeader as BlogInvitationRequest).output(contactId)
    is GroupInvitationRequest -> (messageHeader as GroupInvitationRequest).output(contactId)
    is IntroductionRequest -> (messageHeader as IntroductionRequest).output(contactId)
    // responses
    is ForumInvitationResponse -> (messageHeader as ForumInvitationResponse).output(contactId)
    is BlogInvitationResponse -> (messageHeader as BlogInvitationResponse).output(contactId)
    is GroupInvitationResponse -> (messageHeader as GroupInvitationResponse).output(contactId)
    is IntroductionResponse -> (messageHeader as IntroductionResponse).output(contactId)
    // unknown
    else -> throw IllegalStateException()
}
