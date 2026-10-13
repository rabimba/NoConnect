package org.chagolchana.chagol.transport;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.contact.PendingContactId;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.chagol.api.transport.KeySetId;
import org.chagolchana.chagol.api.transport.StreamContext;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface TransportKeyManager {

	void start(Transaction txn) throws DbException;

	KeySetId addRotationKeys(Transaction txn, ContactId c,
			SecretKey rootKey, long timestamp, boolean alice, boolean active)
			throws DbException;

	KeySetId addHandshakeKeys(Transaction txn, ContactId c,
			SecretKey rootKey, boolean alice) throws DbException;

	KeySetId addHandshakeKeys(Transaction txn, PendingContactId p,
			SecretKey rootKey, boolean alice) throws DbException;

	void activateKeys(Transaction txn, KeySetId k) throws DbException;

	void removeContact(ContactId c);

	void removePendingContact(PendingContactId p);

	boolean canSendOutgoingStreams(ContactId c);

	boolean canSendOutgoingStreams(PendingContactId p);

	@Nullable
	StreamContext getStreamContext(Transaction txn, ContactId c)
			throws DbException;

	@Nullable
	StreamContext getStreamContext(Transaction txn, PendingContactId p)
			throws DbException;

	@Nullable
	StreamContext getStreamContext(Transaction txn, byte[] tag)
			throws DbException;

	@Nullable
	StreamContext getStreamContextOnly(Transaction txn, byte[] tag);

	void markTagAsRecognised(Transaction txn, byte[] tag) throws DbException;

}
