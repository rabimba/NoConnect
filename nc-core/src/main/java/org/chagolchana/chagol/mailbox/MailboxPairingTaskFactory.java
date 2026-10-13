package org.chagolchana.chagol.mailbox;


import org.chagolchana.chagol.api.mailbox.MailboxPairingTask;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MailboxPairingTaskFactory {

	MailboxPairingTask createPairingTask(String qrCodePayload);

}
