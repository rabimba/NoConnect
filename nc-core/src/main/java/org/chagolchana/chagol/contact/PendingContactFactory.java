package org.chagolchana.chagol.contact;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.UnsupportedVersionException;
import org.chagolchana.chagol.api.contact.PendingContact;
import org.chagolchana.chagol.api.crypto.PublicKey;

interface PendingContactFactory {

	/**
	 * Creates a {@link PendingContact} from the given handshake link and alias.
	 *
	 * @throws UnsupportedVersionException If the link uses a format version
	 * that is not supported
	 * @throws FormatException If the link is invalid
	 */
	PendingContact createPendingContact(String link, String alias)
			throws FormatException;

	/**
	 * Creates a handshake link from the given public key.
	 */
	String createHandshakeLink(PublicKey k);
}
