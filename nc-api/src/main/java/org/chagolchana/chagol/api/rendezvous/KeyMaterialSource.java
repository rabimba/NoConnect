package org.chagolchana.chagol.api.rendezvous;

import org.chagolchana.nullsafety.NotNullByDefault;

/**
 * A source of key material for use in making rendezvous connections.
 */
@NotNullByDefault
public interface KeyMaterialSource {

	/**
	 * Returns the requested amount of key material.
	 */
	byte[] getKeyMaterial(int length);
}
