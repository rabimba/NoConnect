package org.chagolchana.noconnect.headless.contact

import org.chagolchana.chagol.api.contact.Contact
import org.chagolchana.chagol.api.contact.event.ContactAddedEvent
import org.chagolchana.chagol.api.plugin.event.ContactConnectedEvent
import org.chagolchana.chagol.api.plugin.event.ContactDisconnectedEvent
import org.chagolchana.chagol.identity.output
import org.chagolchana.noconnect.headless.json.JsonDict

internal fun Contact.output(latestMsgTime: Long, connected: Boolean, unreadCount: Int) = JsonDict(
    "contactId" to id.int,
    "author" to author.output(),
    "verified" to isVerified,
    "lastChatActivity" to latestMsgTime,
    "connected" to connected,
    "unreadCount" to unreadCount
).apply {
    alias?.let { put("alias", it) }
    handshakePublicKey?.let { put("handshakePublicKey", it.encoded) }
}

internal fun ContactAddedEvent.output() = JsonDict(
    "contactId" to contactId.int,
    "verified" to isVerified
)

internal fun ContactConnectedEvent.output() = JsonDict(
    "contactId" to contactId.int
)

internal fun ContactDisconnectedEvent.output() = JsonDict(
    "contactId" to contactId.int
)
