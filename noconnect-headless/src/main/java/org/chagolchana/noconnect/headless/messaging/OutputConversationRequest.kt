package org.chagolchana.noconnect.headless.messaging

import org.chagolchana.chagol.api.contact.ContactId
import org.chagolchana.noconnect.api.blog.BlogInvitationRequest
import org.chagolchana.noconnect.api.conversation.ConversationMessageHeader
import org.chagolchana.noconnect.api.conversation.ConversationRequest
import org.chagolchana.noconnect.api.forum.ForumInvitationRequest
import org.chagolchana.noconnect.api.introduction.IntroductionRequest
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationRequest
import org.chagolchana.noconnect.api.sharing.InvitationRequest
import org.chagolchana.noconnect.headless.json.JsonDict

internal fun ConversationRequest<*>.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationMessageHeader).output(contactId, text)
    dict.putAll(
        "sessionId" to sessionId.bytes,
        "name" to name,
        "answered" to wasAnswered()
    )
    return dict
}

internal fun IntroductionRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationRequest<*>).output(contactId)
    dict.putAll(
        "type" to "IntroductionRequest",
        "alreadyContact" to isContact
    )
    return dict
}

internal fun InvitationRequest<*>.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationRequest<*>).output(contactId)
    dict["canBeOpened"] = canBeOpened()
    return dict
}

internal fun BlogInvitationRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationRequest<*>).output(contactId)
    dict["type"] = "BlogInvitationRequest"
    return dict
}

internal fun ForumInvitationRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationRequest<*>).output(contactId)
    dict["type"] = "ForumInvitationRequest"
    return dict
}

internal fun GroupInvitationRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationRequest<*>).output(contactId)
    dict["type"] = "GroupInvitationRequest"
    return dict
}
