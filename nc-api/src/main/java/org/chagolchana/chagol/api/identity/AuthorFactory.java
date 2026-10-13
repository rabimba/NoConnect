package org.chagolchana.chagol.api.identity;

import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AuthorFactory {

	/**
	 * Creates an author with the current format version and the given name and
	 * public key.
	 */
	Author createAuthor(String name, PublicKey publicKey);

	/**
	 * Creates an author with the given format version, name and public key.
	 */
	Author createAuthor(int formatVersion, String name, PublicKey publicKey);

	/**
	 * Creates a local author with the current format version and the given
	 * name.
	 */
	LocalAuthor createLocalAuthor(String name);
}
