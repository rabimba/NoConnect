package org.chagolchana.noconnect.api.privategroup;

import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.sync.Message;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.noconnect.api.client.ThreadedMessage;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupMessage extends ThreadedMessage {

	public GroupMessage(Message message, @Nullable MessageId parent,
			Author member) {
		super(message, parent, member);
	}

	public Author getMember() {
		return super.getAuthor();
	}

}
