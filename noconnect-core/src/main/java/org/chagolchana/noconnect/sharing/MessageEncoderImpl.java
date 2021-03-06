package org.chagolchana.noconnect.sharing;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.api.sync.MessageId;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.chagolchana.noconnect.sharing.MessageType.ABORT;
import static org.chagolchana.noconnect.sharing.MessageType.ACCEPT;
import static org.chagolchana.noconnect.sharing.MessageType.DECLINE;
import static org.chagolchana.noconnect.sharing.MessageType.INVITE;
import static org.chagolchana.noconnect.sharing.MessageType.LEAVE;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_AVAILABLE_TO_ANSWER;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_INVITATION_ACCEPTED;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_LOCAL;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_MESSAGE_TYPE;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_READ;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_SHAREABLE_ID;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_TIMESTAMP;
import static org.chagolchana.noconnect.sharing.SharingConstants.MSG_KEY_VISIBLE_IN_UI;

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
			GroupId shareableId, long timestamp, boolean local, boolean read,
			boolean visible, boolean available, boolean accepted) {
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_MESSAGE_TYPE, type.getValue());
		meta.put(MSG_KEY_SHAREABLE_ID, shareableId);
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
	public Message encodeInviteMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, BdfList descriptor,
			@Nullable String message) {
		if (message != null && message.equals(""))
			throw new IllegalArgumentException();
		BdfList body = BdfList.of(
				INVITE.getValue(),
				previousMessageId,
				descriptor,
				message
		);
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Message encodeAcceptMessage(GroupId contactGroupId,
			GroupId shareableId, long timestamp,
			@Nullable MessageId previousMessageId) {
		return encodeMessage(ACCEPT, contactGroupId, shareableId, timestamp,
				previousMessageId);
	}

	@Override
	public Message encodeDeclineMessage(GroupId contactGroupId,
			GroupId shareableId, long timestamp,
			@Nullable MessageId previousMessageId) {
		return encodeMessage(DECLINE, contactGroupId, shareableId, timestamp,
				previousMessageId);
	}

	@Override
	public Message encodeLeaveMessage(GroupId contactGroupId,
			GroupId shareableId, long timestamp,
			@Nullable MessageId previousMessageId) {
		return encodeMessage(LEAVE, contactGroupId, shareableId, timestamp,
				previousMessageId);
	}

	@Override
	public Message encodeAbortMessage(GroupId contactGroupId,
			GroupId shareableId, long timestamp,
			@Nullable MessageId previousMessageId) {
		return encodeMessage(ABORT, contactGroupId, shareableId, timestamp,
				previousMessageId);
	}

	private Message encodeMessage(MessageType type, GroupId contactGroupId,
			GroupId shareableId, long timestamp,
			@Nullable MessageId previousMessageId) {
		BdfList body = BdfList.of(
				type.getValue(),
				shareableId,
				previousMessageId
		);
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

}
