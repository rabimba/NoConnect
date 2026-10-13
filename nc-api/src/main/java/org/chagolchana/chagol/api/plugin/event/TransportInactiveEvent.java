package org.chagolchana.chagol.api.plugin.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.plugin.Plugin.State;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a plugin leaves the {@link State#ACTIVE}
 * state.
 */
@Immutable
@NotNullByDefault
public class TransportInactiveEvent extends Event {

	private final TransportId transportId;

	public TransportInactiveEvent(TransportId transportId) {
		this.transportId = transportId;
	}

	public TransportId getTransportId() {
		return transportId;
	}
}
