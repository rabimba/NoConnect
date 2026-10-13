package org.chagolchana.chagol.api.sync;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface GroupFactory {

	/**
	 * Creates a group with the given client ID, major version and descriptor.
	 */
	Group createGroup(ClientId c, int majorVersion, byte[] descriptor);
}
