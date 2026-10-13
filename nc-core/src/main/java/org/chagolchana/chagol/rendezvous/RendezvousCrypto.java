package org.chagolchana.chagol.rendezvous;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.rendezvous.KeyMaterialSource;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RendezvousCrypto {

	SecretKey deriveRendezvousKey(SecretKey staticMasterKey);

	KeyMaterialSource createKeyMaterialSource(SecretKey rendezvousKey,
			TransportId t);
}
