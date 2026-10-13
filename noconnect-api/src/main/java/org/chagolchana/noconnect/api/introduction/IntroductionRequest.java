package org.chagolchana.noconnect.api.introduction;

import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.noconnect.api.client.SessionId;
import org.chagolchana.noconnect.api.conversation.ConversationMessageVisitor;
import org.chagolchana.noconnect.api.conversation.ConversationRequest;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionRequest extends ConversationRequest<Author> {

	private final AuthorInfo authorInfo;

	public IntroductionRequest(MessageId messageId, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, Author author, @Nullable String text,
			boolean answered, AuthorInfo authorInfo, long autoDeleteTimer) {
		super(messageId, groupId, time, local, read, sent, seen, sessionId,
				author, text, answered, autoDeleteTimer);
		this.authorInfo = authorInfo;
	}

	@Nullable
	public String getAlias() {
		return authorInfo.getAlias();
	}

	public boolean isContact() {
		return authorInfo.getStatus().isContact();
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitIntroductionRequest(this);
	}
}
