package org.chagolchana.chagol.api.plugin.simplex;

import org.chagolchana.chagol.api.plugin.Plugin;
import org.chagolchana.chagol.api.plugin.TransportConnectionReader;
import org.chagolchana.chagol.api.plugin.TransportConnectionWriter;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.chagol.api.system.Wakeful;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

/**
 * An interface for transport plugins that support simplex communication.
 */
@NotNullByDefault
public interface SimplexPlugin extends Plugin {

	/**
	 * Returns true if the transport is likely to lose streams and the cost of
	 * transmitting redundant copies of data is cheap.
	 */
	boolean isLossyAndCheap();

	/**
	 * Attempts to create and return a reader for the given transport
	 * properties. Returns null if a reader cannot be created.
	 */
	@Wakeful
	@Nullable
	TransportConnectionReader createReader(TransportProperties p);

	/**
	 * Attempts to create and return a writer for the given transport
	 * properties. Returns null if a writer cannot be created.
	 */
	@Wakeful
	@Nullable
	TransportConnectionWriter createWriter(TransportProperties p);
}
