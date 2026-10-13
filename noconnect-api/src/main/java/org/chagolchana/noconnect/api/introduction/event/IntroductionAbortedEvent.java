package org.chagolchana.noconnect.api.introduction.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.noconnect.api.client.SessionId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionAbortedEvent extends Event {

	private final SessionId sessionId;

	public IntroductionAbortedEvent(SessionId sessionId) {
		this.sessionId = sessionId;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

}
