package org.chagolchana.noconnect.introduction;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.noconnect.api.client.SessionId;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MessageParser {

	BdfDictionary getMessagesVisibleInUiQuery();

	BdfDictionary getRequestsAvailableToAnswerQuery(SessionId sessionId);

	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;

	RequestMessage parseRequestMessage(Message m, BdfList body)
			throws FormatException;

	AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException;

	DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException;

	AuthMessage parseAuthMessage(Message m, BdfList body)
			throws FormatException;

	ActivateMessage parseActivateMessage(Message m, BdfList body)
			throws FormatException;

	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;

}
