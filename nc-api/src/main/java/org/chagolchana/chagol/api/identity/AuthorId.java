package org.chagolchana.chagol.api.identity;

import org.chagolchana.chagol.api.UniqueId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies an
 * {@link Author}.
 */
@ThreadSafe
@NotNullByDefault
public class AuthorId extends UniqueId {

	/**
	 * Label for hashing authors to calculate their identities.
	 */
	public static final String LABEL = "org.chagolchana.chagol/AUTHOR_ID";

	public AuthorId(byte[] id) {
		super(id);
	}
}
