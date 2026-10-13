package org.chagolchana.chagol.transport;

import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface TransportKeyManagerFactory {

	TransportKeyManager createTransportKeyManager(TransportId transportId,
			long maxLatency);

}
