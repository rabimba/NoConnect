package org.chagolchana.chagol.api.plugin;

/**
 * Bluetooth LE transport (experimental, gated by
 * FeatureFlags.shouldEnableBleTransport()).
 */
public interface BleConstants {

	TransportId ID = new TransportId("org.chagolchana.chagol.ble");

	/**
	 * Service UUID under which the rendezvous token and L2CAP channel number
	 * are advertised as service data.
	 */
	String SERVICE_UUID = "6e630b1e-5a3c-4c55-9b0e-2f6b8e0c7a41";

	/**
	 * Transport property holding this device's rendezvous secret (hex). It is
	 * shared only with contacts, through the encrypted transport-properties
	 * sync, and lets them recognise this device's rotating tokens.
	 */
	String PROP_SECRET = "secret";

	int SECRET_BYTES = 32;

	boolean DEFAULT_PREF_PLUGIN_ENABLE = false;
}
