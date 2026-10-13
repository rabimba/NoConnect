package org.chagolchana.noconnect.android.contact;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.sync.Group;
import org.chagolchana.chagol.test.ChagolMockTestCase;
import org.chagolchana.chagol.test.DbExpectations;
import org.chagolchana.noconnect.api.autodelete.AutoDeleteManager;
import org.chagolchana.noconnect.api.conversation.ConversationManager;
import org.chagolchana.noconnect.api.messaging.MessagingManager;
import org.chagolchana.noconnect.api.messaging.PrivateMessage;
import org.chagolchana.noconnect.api.messaging.PrivateMessageFactory;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.chagolchana.chagol.test.TestUtils.getContact;
import static org.chagolchana.chagol.test.TestUtils.getGroup;
import static org.chagolchana.chagol.test.TestUtils.getMessage;
import static org.chagolchana.noconnect.api.messaging.MessagingManager.CLIENT_ID;
import static org.chagolchana.noconnect.api.messaging.MessagingManager.MAJOR_VERSION;
import static org.junit.Assert.assertEquals;

public class CheckInSenderTest extends ChagolMockTestCase {

	private final TransactionManager db =
			context.mock(TransactionManager.class);
	private final ContactManager contactManager =
			context.mock(ContactManager.class);
	private final MessagingManager messagingManager =
			context.mock(MessagingManager.class);
	private final ConversationManager conversationManager =
			context.mock(ConversationManager.class);
	private final AutoDeleteManager autoDeleteManager =
			context.mock(AutoDeleteManager.class);
	private final PrivateMessageFactory privateMessageFactory =
			context.mock(PrivateMessageFactory.class);

	private final CheckInSender sender = new CheckInSender(db,
			contactManager, messagingManager, conversationManager,
			autoDeleteManager, privateMessageFactory);

	@Test
	public void testSendsToEveryContactWithItsTimer() throws Exception {
		Transaction txn = new Transaction(null, false);
		Contact c1 = getContact(), c2 = getContact();
		Group g1 = getGroup(CLIENT_ID, MAJOR_VERSION);
		Group g2 = getGroup(CLIENT_ID, MAJOR_VERSION);
		PrivateMessage m1 = new PrivateMessage(getMessage(g1.getId()));
		PrivateMessage m2 = new PrivateMessage(getMessage(g2.getId()));
		String text = "I'm safe";

		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(false), withDbCallable(txn));
			oneOf(contactManager).getContacts(txn);
			will(returnValue(asList(c1, c2)));
			oneOf(messagingManager).getContactGroup(c1);
			will(returnValue(g1));
			oneOf(conversationManager)
					.getTimestampForOutgoingMessage(txn, c1.getId());
			will(returnValue(1000L));
			oneOf(autoDeleteManager)
					.getAutoDeleteTimer(txn, c1.getId(), 1000L);
			will(returnValue(0L));
			oneOf(privateMessageFactory).createPrivateMessage(g1.getId(),
					1000L, text, emptyList(), 0L);
			will(returnValue(m1));
			oneOf(messagingManager).addLocalMessage(txn, m1);
			oneOf(messagingManager).getContactGroup(c2);
			will(returnValue(g2));
			oneOf(conversationManager)
					.getTimestampForOutgoingMessage(txn, c2.getId());
			will(returnValue(2000L));
			oneOf(autoDeleteManager)
					.getAutoDeleteTimer(txn, c2.getId(), 2000L);
			will(returnValue(86_400_000L));
			oneOf(privateMessageFactory).createPrivateMessage(g2.getId(),
					2000L, text, emptyList(), 86_400_000L);
			will(returnValue(m2));
			oneOf(messagingManager).addLocalMessage(txn, m2);
		}});

		assertEquals(2, sender.sendToAllContacts(text));
	}

	@Test
	public void testNoContactsSendsNothing() throws Exception {
		Transaction txn = new Transaction(null, false);
		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(false), withDbCallable(txn));
			oneOf(contactManager).getContacts(txn);
			will(returnValue(emptyList()));
		}});
		assertEquals(0, sender.sendToAllContacts("I need help"));
	}
}
