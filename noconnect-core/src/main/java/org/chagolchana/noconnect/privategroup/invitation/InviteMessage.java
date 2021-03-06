package org.chagolchana.noconnect.privategroup.invitation;

import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class InviteMessage extends GroupInvitationMessage {

	private final String groupName;
	private final Author creator;
	private final byte[] salt, signature;
	@Nullable
	private final String message;

	InviteMessage(MessageId id, GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, String groupName, Author creator, byte[] salt,
			@Nullable String message, byte[] signature) {
		super(id, contactGroupId, privateGroupId, timestamp);
		this.groupName = groupName;
		this.creator = creator;
		this.salt = salt;
		this.message = message;
		this.signature = signature;
	}

	String getGroupName() {
		return groupName;
	}

	Author getCreator() {
		return creator;
	}

	byte[] getSalt() {
		return salt;
	}

	@Nullable
	String getMessage() {
		return message;
	}

	byte[] getSignature() {
		return signature;
	}
}
