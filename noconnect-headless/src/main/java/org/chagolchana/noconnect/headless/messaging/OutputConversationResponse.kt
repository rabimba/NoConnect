package org.chagolchana.noconnect.headless.messaging

import org.chagolchana.chagol.api.contact.ContactId
import org.chagolchana.chagol.identity.output
import org.chagolchana.noconnect.api.blog.BlogInvitationResponse
import org.chagolchana.noconnect.api.conversation.ConversationMessageHeader
import org.chagolchana.noconnect.api.conversation.ConversationResponse
import org.chagolchana.noconnect.api.forum.ForumInvitationResponse
import org.chagolchana.noconnect.api.introduction.IntroductionResponse
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationResponse
import org.chagolchana.noconnect.api.sharing.InvitationResponse
import org.chagolchana.noconnect.headless.json.JsonDict

internal fun ConversationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationMessageHeader).output(contactId)
    dict.putAll(
        "sessionId" to sessionId.bytes,
        "accepted" to wasAccepted()
    )
    return dict
}

internal fun IntroductionResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationResponse).output(contactId)
    dict.putAll(
        "type" to "IntroductionResponse",
        "introducedAuthor" to introducedAuthor.output(),
        "introducer" to isIntroducer
    )
    return dict
}

internal fun InvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationResponse).output(contactId)
    dict["shareableId"] = shareableId.bytes
    return dict
}

internal fun BlogInvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationResponse).output(contactId)
    dict["type"] = "BlogInvitationResponse"
    return dict
}

internal fun ForumInvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationResponse).output(contactId)
    dict["type"] = "ForumInvitationResponse"
    return dict
}

internal fun GroupInvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationResponse).output(contactId)
    dict["type"] = "GroupInvitationResponse"
    return dict
}
