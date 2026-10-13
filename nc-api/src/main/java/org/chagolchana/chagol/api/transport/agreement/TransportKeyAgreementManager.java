package org.chagolchana.chagol.api.transport.agreement;

import org.chagolchana.chagol.api.sync.ClientId;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface TransportKeyAgreementManager {

	/**
	 * The unique ID of the transport key agreement client.
	 */
	ClientId CLIENT_ID =
			new ClientId("org.chagolchana.chagol.transport.agreement");

	/**
	 * The current major version of the transport key agreement client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the transport key agreement client.
	 */
	int MINOR_VERSION = 0;
}
