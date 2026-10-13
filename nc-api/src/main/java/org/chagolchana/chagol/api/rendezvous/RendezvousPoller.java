package org.chagolchana.chagol.api.rendezvous;

import org.chagolchana.chagol.api.contact.PendingContactId;

/**
 * Interface for the poller that makes rendezvous connections to pending
 * contacts.
 */
public interface RendezvousPoller {

	long getLastPollTime(PendingContactId p);
}
