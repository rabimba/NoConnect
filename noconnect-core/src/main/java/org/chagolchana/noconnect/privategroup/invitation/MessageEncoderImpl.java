package org.chagolchana.noconnect.privategroup.invitation;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.api.sync.MessageId;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.chagolchana.noconnect.client.MessageTrackerConstants.MSG_KEY_READ;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_AVAILABLE_TO_ANSWER;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_INVITATION_ACCEPTED;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_LOCAL;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_MESSAGE_TYPE;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_PRIVATE_GROUP_ID;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_TIMESTAMP;
import static org.chagolchana.noconnect.privategroup.invitation.GroupInvitationConstants.MSG_KEY_VISIBLE_IN_UI;
import static org.chagolchana.noconnect.privategroup.invitation.MessageType.ABORT;
import static org.chagolchana.noconnect.privategroup.invitation.MessageType.INVITE;
import static org.chagolchana.noconnect.privategroup.invitation.MessageType.JOIN;
import static org.chagolchana.noconnect.privategroup.invitation.MessageType.LEAVE;

@Immutable
@NotNullByDefault
class MessageEncoderImpl implements MessageEncoder {

	private final ClientHelper clientHelper;
	private final MessageFactory messageFactory;

	@Inject
	MessageEncoderImpl(ClientHelper clientHelper,
			MessageFactory messageFactory) {
		this.clientHelper = clientHelper;
		this.messageFactory = messageFactory;
	}

	@Override
	public BdfDictionary encodeMetadata(MessageType type,
			GroupId privateGroupId, long timestamp, boolean local, boolean read,
			boolean visible, boolean available, boolean accepted) {
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_MESSAGE_TYPE, type.getValue());
		meta.put(MSG_KEY_PRIVATE_GROUP_ID, privateGroupId);
		meta.put(MSG_KEY_TIMESTAMP, timestamp);
		meta.put(MSG_KEY_LOCAL, local);
		meta.put(MSG_KEY_READ, read);
		meta.put(MSG_KEY_VISIBLE_IN_UI, visible);
		meta.put(MSG_KEY_AVAILABLE_TO_ANSWER, available);
		meta.put(MSG_KEY_INVITATION_ACCEPTED, accepted);
		return meta;
	}

	@Override
	public void setVisibleInUi(BdfDictionary meta, boolean visible) {
		meta.put(MSG_KEY_VISIBLE_IN_UI, visible);
	}

	@Override
	public void setAvailableToAnswer(BdfDictionary meta, boolean available) {
		meta.put(MSG_KEY_AVAILABLE_TO_ANSWER, available);
	}

	@Override
	public void setInvitationAccepted(BdfDictionary meta, boolean accepted) {
		meta.put(MSG_KEY_INVITATION_ACCEPTED, accepted);
	}

	@Override
	public Message encodeInviteMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp, String groupName,
			Author creator, byte[] salt, @Nullable String message,
			byte[] signature) {
		BdfList body = BdfList.of(
				INVITE.getValue(),
				groupName,
				creator.getName(),
				creator.getPublicKey(),
				salt,
				message,
				signature
		);
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Message encodeJoinMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp,
			@Nullable MessageId previousMessageId) {
		BdfList body = BdfList.of(
				JOIN.getValue(),
				privateGroupId,
				previousMessageId
		);
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Message encodeLeaveMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp,
			@Nullable MessageId previousMessageId) {
		BdfList body = BdfList.of(
				LEAVE.getValue(),
				privateGroupId,
				previousMessageId
		);
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Message encodeAbortMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp) {
		BdfList body = BdfList.of(
				ABORT.getValue(),
				privateGroupId
		);
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
}
