package org.chagolchana.chagol.api.sync;

import org.chagolchana.chagol.api.UniqueId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a
 * {@link Message}.
 */
@ThreadSafe
@NotNullByDefault
public class MessageId extends UniqueId {

	/**
	 * Label for hashing messages to calculate their identifiers.
	 */
	public static final String ID_LABEL = "org.chagolchana.chagol/MESSAGE_ID";

	/**
	 * Label for hashing blocks of messages.
	 */
	public static final String BLOCK_LABEL =
			"org.chagolchana.chagol/MESSAGE_BLOCK";

	public MessageId(byte[] id) {
		super(id);
	}
}
