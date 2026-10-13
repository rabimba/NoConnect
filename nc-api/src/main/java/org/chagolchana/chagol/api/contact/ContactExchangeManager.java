package org.chagolchana.chagol.api.contact;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.db.ContactExistsException;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface ContactExchangeManager {

	/**
	 * Exchanges contact information with a remote peer and adds the peer
	 * as a contact.
	 *
	 * @param alice Whether the local peer takes the role of Alice
	 * @return The newly added contact
	 * @throws ContactExistsException If the contact already exists
	 */
	Contact exchangeContacts(DuplexTransportConnection conn,
			SecretKey masterKey, boolean alice, boolean verified)
			throws IOException, DbException;

	/**
	 * Exchanges contact information with a remote peer and adds the peer
	 * as a contact, replacing the given pending contact.
	 *
	 * @param alice Whether the local peer takes the role of Alice
	 * @return The newly added contact
	 * @throws ContactExistsException If the contact already exists
	 */
	Contact exchangeContacts(PendingContactId p, DuplexTransportConnection conn,
			SecretKey masterKey, boolean alice, boolean verified)
			throws IOException, DbException;
}
