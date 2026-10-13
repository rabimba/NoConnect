package org.chagolchana.noconnect.privategroup.invitation;

import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
abstract class DeletableGroupInvitationMessage extends GroupInvitationMessage {

	private final long autoDeleteTimer;

	DeletableGroupInvitationMessage(MessageId id, GroupId contactGroupId,
			GroupId privateGroupId, long timestamp, long autoDeleteTimer) {
		super(id, contactGroupId, privateGroupId, timestamp);
		this.autoDeleteTimer = autoDeleteTimer;
	}

	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
}
