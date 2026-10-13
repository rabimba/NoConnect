package org.chagolchana.noconnect.introduction;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.test.ChagolMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import static org.chagolchana.chagol.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static org.chagolchana.chagol.test.TestUtils.getAuthor;
import static org.chagolchana.chagol.test.TestUtils.getMessage;
import static org.chagolchana.chagol.test.TestUtils.getRandomId;
import static org.chagolchana.chagol.util.StringUtils.getRandomString;
import static org.chagolchana.noconnect.api.introduction.IntroductionConstants.MAX_INTRODUCTION_TEXT_LENGTH;
import static org.chagolchana.noconnect.introduction.MessageType.REQUEST;

public class MessageEncoderTest extends ChagolMockTestCase {

	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final MessageFactory messageFactory =
			context.mock(MessageFactory.class);
	private final MessageEncoder messageEncoder =
			new MessageEncoderImpl(clientHelper, messageFactory);

	private final GroupId groupId = new GroupId(getRandomId());
	private final Message message =
			getMessage(groupId, MAX_MESSAGE_BODY_LENGTH);
	private final long timestamp = message.getTimestamp();
	private final byte[] body = message.getBody();
	private final Author author = getAuthor();
	private final BdfList authorList = new BdfList();
	private final String text = getRandomString(MAX_INTRODUCTION_TEXT_LENGTH);

	@Test
	public void testEncodeRequestMessage() throws FormatException {
		context.checking(new Expectations() {{
			oneOf(clientHelper).toList(author);
			will(returnValue(authorList));
		}});
		expectCreateMessage(
				BdfList.of(REQUEST.getValue(), null, authorList, text));

		messageEncoder.encodeRequestMessage(groupId, timestamp, null,
				author, text);
	}

	private void expectCreateMessage(BdfList bodyList) throws FormatException {
		context.checking(new Expectations() {{
			oneOf(clientHelper).toByteArray(bodyList);
			will(returnValue(body));
			oneOf(messageFactory).createMessage(groupId, timestamp, body);
			will(returnValue(message));
		}});
	}

}
