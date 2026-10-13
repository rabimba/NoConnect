package org.chagolchana.chagol.api.contact;

import org.chagolchana.chagol.api.UniqueId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a
 * {@link PendingContact}.
 */
@ThreadSafe
@NotNullByDefault
public class PendingContactId extends UniqueId {

	public PendingContactId(byte[] id) {
		super(id);
	}
}
