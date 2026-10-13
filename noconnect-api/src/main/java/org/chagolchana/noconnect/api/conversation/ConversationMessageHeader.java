package org.chagolchana.noconnect.api.conversation;

import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class ConversationMessageHeader {

	private final MessageId id;
	private final GroupId groupId;
	private final long timestamp, autoDeleteTimer;
	private final boolean local, read, sent, seen;

	public ConversationMessageHeader(MessageId id, GroupId groupId,
			long timestamp, boolean local, boolean read, boolean sent,
			boolean seen, long autoDeleteTimer) {
		this.id = id;
		this.groupId = groupId;
		this.timestamp = timestamp;
		this.local = local;
		this.read = read;
		this.sent = sent;
		this.seen = seen;
		this.autoDeleteTimer = autoDeleteTimer;
	}

	public MessageId getId() {
		return id;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isSent() {
		return sent;
	}

	public boolean isSeen() {
		return seen;
	}

	public boolean isRead() {
		return read;
	}

	public abstract <T> T accept(ConversationMessageVisitor<T> v);

	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
}
