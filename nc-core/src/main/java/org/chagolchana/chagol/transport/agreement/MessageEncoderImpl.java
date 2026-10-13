package org.chagolchana.chagol.transport.agreement;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfEntry;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.chagolchana.chagol.transport.agreement.MessageType.ACTIVATE;
import static org.chagolchana.chagol.transport.agreement.MessageType.KEY;
import static org.chagolchana.chagol.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_IS_SESSION;
import static org.chagolchana.chagol.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_LOCAL;
import static org.chagolchana.chagol.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_MESSAGE_TYPE;
import static org.chagolchana.chagol.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_TRANSPORT_ID;

@Immutable
@NotNullByDefault
class MessageEncoderImpl implements MessageEncoder {

	private final ClientHelper clientHelper;
	private final Clock clock;

	@Inject
	MessageEncoderImpl(ClientHelper clientHelper, Clock clock) {
		this.clientHelper = clientHelper;
		this.clock = clock;
	}

	@Override
	public Message encodeKeyMessage(GroupId contactGroupId,
			TransportId transportId, PublicKey publicKey) {
		BdfList body = BdfList.of(
				KEY.getValue(),
				transportId.getString(),
				publicKey.getEncoded());
		return encodeMessage(contactGroupId, body);
	}

	@Override
	public Message encodeActivateMessage(GroupId contactGroupId,
			TransportId transportId, MessageId previousMessageId) {
		BdfList body = BdfList.of(
				ACTIVATE.getValue(),
				transportId.getString(),
				previousMessageId);
		return encodeMessage(contactGroupId, body);
	}

	@Override
	public BdfDictionary encodeMessageMetadata(TransportId transportId,
			MessageType type, boolean local) {
		return BdfDictionary.of(
				new BdfEntry(MSG_KEY_IS_SESSION, false),
				new BdfEntry(MSG_KEY_TRANSPORT_ID, transportId.getString()),
				new BdfEntry(MSG_KEY_MESSAGE_TYPE, type.getValue()),
				new BdfEntry(MSG_KEY_LOCAL, local));
	}

	private Message encodeMessage(GroupId contactGroupId, BdfList body) {
		try {
			return clientHelper.createMessage(contactGroupId,
					clock.currentTimeMillis(), clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError();
		}
	}
}
