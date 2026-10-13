package org.chagolchana.chagol.api.network;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface NetworkManager {

	NetworkStatus getNetworkStatus();
}
