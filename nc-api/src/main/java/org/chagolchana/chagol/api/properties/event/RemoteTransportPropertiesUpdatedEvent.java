package org.chagolchana.chagol.api.properties.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when {@link TransportProperties} are received
 * from a contact.
 */
@Immutable
@NotNullByDefault
public class RemoteTransportPropertiesUpdatedEvent extends Event {

	private final TransportId transportId;

	public RemoteTransportPropertiesUpdatedEvent(TransportId transportId) {
		this.transportId = transportId;
	}

	public TransportId getTransportId() {
		return transportId;
	}
}
