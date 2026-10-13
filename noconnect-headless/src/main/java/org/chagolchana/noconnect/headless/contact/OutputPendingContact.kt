package org.chagolchana.noconnect.headless.contact

import org.chagolchana.chagol.api.contact.PendingContact
import org.chagolchana.chagol.api.contact.PendingContactState
import org.chagolchana.chagol.api.contact.PendingContactState.ADDING_CONTACT
import org.chagolchana.chagol.api.contact.PendingContactState.CONNECTING
import org.chagolchana.chagol.api.contact.PendingContactState.FAILED
import org.chagolchana.chagol.api.contact.PendingContactState.OFFLINE
import org.chagolchana.chagol.api.contact.PendingContactState.WAITING_FOR_CONNECTION
import org.chagolchana.chagol.api.contact.event.PendingContactAddedEvent
import org.chagolchana.chagol.api.contact.event.PendingContactRemovedEvent
import org.chagolchana.chagol.api.contact.event.PendingContactStateChangedEvent
import org.chagolchana.noconnect.headless.json.JsonDict

internal fun PendingContact.output() = JsonDict(
    "pendingContactId" to id.bytes,
    "alias" to alias,
    "timestamp" to timestamp
)

internal fun PendingContactState.output() = when (this) {
    WAITING_FOR_CONNECTION -> "waiting_for_connection"
    OFFLINE -> "offline"
    CONNECTING -> "connecting"
    ADDING_CONTACT -> "adding_contact"
    FAILED -> "failed"
    else -> throw AssertionError()
}

internal fun PendingContactAddedEvent.output() = JsonDict(
    "pendingContact" to pendingContact.output()
)

internal fun PendingContactStateChangedEvent.output() = JsonDict(
    "pendingContactId" to id.bytes,
    "state" to pendingContactState.output()
)

internal fun PendingContactRemovedEvent.output() = JsonDict(
    "pendingContactId" to id.bytes
)
