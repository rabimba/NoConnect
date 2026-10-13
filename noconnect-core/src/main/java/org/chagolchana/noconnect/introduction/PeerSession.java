package org.chagolchana.noconnect.introduction;

import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.noconnect.api.client.SessionId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface PeerSession {

	SessionId getSessionId();

	GroupId getContactGroupId();

	long getLocalTimestamp();

	@Nullable
	MessageId getLastLocalMessageId();

	@Nullable
	MessageId getLastRemoteMessageId();

}
