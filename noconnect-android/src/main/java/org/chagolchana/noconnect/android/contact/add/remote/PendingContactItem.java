package org.chagolchana.noconnect.android.contact.add.remote;

import org.chagolchana.chagol.api.contact.PendingContact;
import org.chagolchana.chagol.api.contact.PendingContactState;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static org.chagolchana.chagol.api.contact.PendingContactState.CONNECTING;
import static org.chagolchana.chagol.api.contact.PendingContactState.WAITING_FOR_CONNECTION;

@Immutable
@NotNullByDefault
class PendingContactItem {

	static final int POLL_DURATION_MS = 15_000;

	private final PendingContact pendingContact;
	private final PendingContactState state;
	private final long lastPoll;

	PendingContactItem(PendingContact pendingContact,
			PendingContactState state, long lastPoll) {
		this.pendingContact = pendingContact;
		this.state = state;
		this.lastPoll = lastPoll;
	}

	PendingContact getPendingContact() {
		return pendingContact;
	}

	PendingContactState getState() {
		if (state == WAITING_FOR_CONNECTION &&
				System.currentTimeMillis() - lastPoll < POLL_DURATION_MS) {
			return CONNECTING;
		}
		return state;
	}
}
