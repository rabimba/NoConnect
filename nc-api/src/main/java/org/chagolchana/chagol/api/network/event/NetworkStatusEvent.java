package org.chagolchana.chagol.api.network.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.network.NetworkStatus;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class NetworkStatusEvent extends Event {

	private final NetworkStatus status;

	public NetworkStatusEvent(NetworkStatus status) {
		this.status = status;
	}

	public NetworkStatus getStatus() {
		return status;
	}
}