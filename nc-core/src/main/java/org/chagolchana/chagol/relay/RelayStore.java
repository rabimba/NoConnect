package org.chagolchana.chagol.relay;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.Nullable;

@NotNullByDefault
public interface RelayStore {

	long DEFAULT_CAPACITY_BYTES = 64L * 1024 * 1024; // 64 MB

	void put(SealedEnvelope envelope);

	@Nullable
	SealedEnvelope get(EnvelopeId id);

	boolean has(EnvelopeId id);

	boolean remove(EnvelopeId id);

	Collection<SealedEnvelope> getAll();

	int getCount();

	long getByteSize();

	void pruneExpired(long currentTimestampSeconds);

	void evictToCapacity(long maxBytes);
}
