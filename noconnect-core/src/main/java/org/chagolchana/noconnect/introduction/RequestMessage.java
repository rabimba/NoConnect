package org.chagolchana.noconnect.introduction;

import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class RequestMessage extends AbstractIntroductionMessage {

	private final Author author;
	@Nullable
	private final String text;

	RequestMessage(MessageId messageId, GroupId groupId, long timestamp,
			@Nullable MessageId previousMessageId, Author author,
			@Nullable String text, long autoDeleteTimer) {
		super(messageId, groupId, timestamp, previousMessageId,
				autoDeleteTimer);
		this.author = author;
		this.text = text;
	}

	public Author getAuthor() {
		return author;
	}

	@Nullable
	public String getText() {
		return text;
	}

}
