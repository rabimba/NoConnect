package org.chagolchana.noconnect.api.avatar;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.chagol.api.sync.ClientId;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

@NotNullByDefault
public interface AvatarManager {

	/**
	 * The unique ID of the avatar client.
	 */
	ClientId CLIENT_ID = new ClientId("org.chagolchana.noconnect.avatar");

	/**
	 * The current major version of the avatar client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the avatar client.
	 */
	int MINOR_VERSION = 0;

	/**
	 * Store a new profile image represented by the given InputStream
	 * and share it with all contacts.
	 */
	AttachmentHeader addAvatar(String contentType, InputStream in)
			throws DbException, IOException;

	/**
	 * Returns the current known profile image header for the given contact
	 * or null if none is known.
	 */
	@Nullable
	AttachmentHeader getAvatarHeader(Transaction txn, Contact c)
			throws DbException;

	/**
	 * Returns our current profile image header or null if none has been added.
	 */
	@Nullable
	AttachmentHeader getMyAvatarHeader(Transaction txn) throws DbException;
}
