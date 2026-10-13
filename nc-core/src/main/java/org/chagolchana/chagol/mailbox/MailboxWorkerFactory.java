package org.chagolchana.chagol.mailbox;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.mailbox.MailboxFolderId;
import org.chagolchana.chagol.api.mailbox.MailboxProperties;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
interface MailboxWorkerFactory {

	MailboxWorker createUploadWorker(ConnectivityChecker connectivityChecker,
			MailboxProperties properties, MailboxFolderId folderId,
			ContactId contactId);

	MailboxWorker createDownloadWorkerForContactMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties);

	MailboxWorker createDownloadWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties);

	MailboxWorker createContactListWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			MailboxProperties properties);
}
