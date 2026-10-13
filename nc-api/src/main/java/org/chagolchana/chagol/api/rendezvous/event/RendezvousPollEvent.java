package org.chagolchana.chagol.api.rendezvous.event;

import org.chagolchana.chagol.api.contact.PendingContactId;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a transport plugin is polled for connections
 * to one or more pending contacts.
 */
@Immutable
@NotNullByDefault
public class RendezvousPollEvent extends Event {

	private final TransportId transportId;
	private final Collection<PendingContactId> pendingContacts;

	public RendezvousPollEvent(TransportId transportId,
			Collection<PendingContactId> pendingContacts) {
		this.transportId = transportId;
		this.pendingContacts = pendingContacts;
	}

	public TransportId getTransportId() {
		return transportId;
	}

	public Collection<PendingContactId> getPendingContacts() {
		return pendingContacts;
	}
}
