package org.chagolchana.chagol.api.sync;

import org.chagolchana.nullsafety.NotNullByDefault;

/**
 * An interface for handling a {@link Priority} record received by an
 * incoming {@link SyncSession}.
 */
@NotNullByDefault
public interface PriorityHandler {

	void handle(Priority p);
}
