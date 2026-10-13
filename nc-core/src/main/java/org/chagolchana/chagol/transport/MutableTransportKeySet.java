package org.chagolchana.chagol.transport;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.contact.PendingContactId;
import org.chagolchana.chagol.api.transport.KeySetId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static org.chagolchana.nullsafety.NullSafety.requireExactlyOneNull;

@NotThreadSafe
@NotNullByDefault
class MutableTransportKeySet {

	private final KeySetId keySetId;
	@Nullable
	private final ContactId contactId;
	@Nullable
	private final PendingContactId pendingContactId;
	private final MutableTransportKeys keys;

	MutableTransportKeySet(KeySetId keySetId, @Nullable ContactId contactId,
			@Nullable PendingContactId pendingContactId,
			MutableTransportKeys keys) {
		requireExactlyOneNull(contactId, pendingContactId);
		this.keySetId = keySetId;
		this.contactId = contactId;
		this.pendingContactId = pendingContactId;
		this.keys = keys;
	}

	KeySetId getKeySetId() {
		return keySetId;
	}

	@Nullable
	ContactId getContactId() {
		return contactId;
	}

	@Nullable
	PendingContactId getPendingContactId() {
		return pendingContactId;
	}

	MutableTransportKeys getKeys() {
		return keys;
	}
}
