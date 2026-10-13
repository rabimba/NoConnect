package org.chagolchana.chagol.mailbox;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.client.BdfMessageContext;
import org.chagolchana.chagol.api.client.BdfMessageValidator;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.data.MetadataEncoder;
import org.chagolchana.chagol.api.sync.Group;
import org.chagolchana.chagol.api.sync.InvalidMessageException;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static org.chagolchana.chagol.api.mailbox.MailboxUpdateManager.MSG_KEY_LOCAL;
import static org.chagolchana.chagol.api.mailbox.MailboxUpdateManager.MSG_KEY_VERSION;
import static org.chagolchana.chagol.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class MailboxUpdateValidator extends BdfMessageValidator {

	MailboxUpdateValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException {
		// Version, Properties, clientSupports, serverSupports
		checkSize(body, 4);
		// Version
		long version = body.getLong(0);
		if (version < 0) throw new FormatException();
		// clientSupports
		BdfList clientSupports = body.getList(1);
		// serverSupports
		BdfList serverSupports = body.getList(2);
		// Properties
		BdfDictionary dictionary = body.getDictionary(3);
		clientHelper.parseAndValidateMailboxUpdate(clientSupports,
				serverSupports, dictionary);
		// Return the metadata
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_LOCAL, false);
		return new BdfMessageContext(meta);
	}

}
