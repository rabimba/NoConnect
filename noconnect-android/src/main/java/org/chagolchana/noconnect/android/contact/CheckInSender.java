package org.chagolchana.noconnect.android.contact;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.api.autodelete.AutoDeleteManager;
import org.chagolchana.noconnect.api.conversation.ConversationManager;
import org.chagolchana.noconnect.api.messaging.MessagingManager;
import org.chagolchana.noconnect.api.messaging.PrivateMessage;
import org.chagolchana.noconnect.api.messaging.PrivateMessageFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.inject.Inject;

import static java.util.Collections.emptyList;

/**
 * Sends the same short check-in message ("I'm safe", "I need help") to every
 * contact as an ordinary private message, so it needs no new protocol and
 * reaches each contact the next time their device can be reached. Each
 * message uses that conversation's disappearing-message timer.
 */
@NotNullByDefault
class CheckInSender {

	private final TransactionManager db;
	private final ContactManager contactManager;
	private final MessagingManager messagingManager;
	private final ConversationManager conversationManager;
	private final AutoDeleteManager autoDeleteManager;
	private final PrivateMessageFactory privateMessageFactory;

	@Inject
	CheckInSender(TransactionManager db, ContactManager contactManager,
			MessagingManager messagingManager,
			ConversationManager conversationManager,
			AutoDeleteManager autoDeleteManager,
			PrivateMessageFactory privateMessageFactory) {
		this.db = db;
		this.contactManager = contactManager;
		this.messagingManager = messagingManager;
		this.conversationManager = conversationManager;
		this.autoDeleteManager = autoDeleteManager;
		this.privateMessageFactory = privateMessageFactory;
	}

	/**
	 * Sends the given text to all contacts in one transaction and returns the
	 * number of contacts it was sent to.
	 */
	int sendToAllContacts(String text) throws DbException {
		return db.transactionWithResult(false, txn -> {
			int sent = 0;
			for (Contact c : contactManager.getContacts(txn)) {
				GroupId g = messagingManager.getContactGroup(c).getId();
				long timestamp = conversationManager
						.getTimestampForOutgoingMessage(txn, c.getId());
				long timer = autoDeleteManager
						.getAutoDeleteTimer(txn, c.getId(), timestamp);
				PrivateMessage m;
				try {
					m = privateMessageFactory.createPrivateMessage(g,
							timestamp, text, emptyList(), timer);
				} catch (FormatException e) {
					throw new AssertionError(e);
				}
				messagingManager.addLocalMessage(txn, m);
				sent++;
			}
			return sent;
		});
	}
}
