package org.chagolchana.chagol.relay;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An in-memory, bounded thread-safe store for transit relay envelopes.
 */
@ThreadSafe
@NotNullByDefault
public class MemoryRelayStore implements RelayStore {

	private final long maxCapacityBytes;
	private final Map<EnvelopeId, SealedEnvelope> store = new LinkedHashMap<>();
	private long currentByteSize = 0;

	public MemoryRelayStore() {
		this(DEFAULT_CAPACITY_BYTES);
	}

	public MemoryRelayStore(long maxCapacityBytes) {
		this.maxCapacityBytes = maxCapacityBytes;
	}

	private static long envelopeSize(SealedEnvelope env) {
		return SealedEnvelope.HEADER_BYTES + env.getPaddedCiphertext().length;
	}

	@Override
	public synchronized void put(SealedEnvelope envelope) {
		SealedEnvelope existing = store.get(envelope.getId());
		if (existing != null) {
			if (envelope.getCopies() > existing.getCopies()) {
				// Update with higher copy count
				store.put(envelope.getId(), envelope);
			}
			return;
		}

		long size = envelopeSize(envelope);
		store.put(envelope.getId(), envelope);
		currentByteSize += size;

		evictToCapacity(maxCapacityBytes);
	}

	@Override
	@Nullable
	public synchronized SealedEnvelope get(EnvelopeId id) {
		return store.get(id);
	}

	@Override
	public synchronized boolean has(EnvelopeId id) {
		return store.containsKey(id);
	}

	@Override
	public synchronized boolean remove(EnvelopeId id) {
		SealedEnvelope removed = store.remove(id);
		if (removed != null) {
			currentByteSize -= envelopeSize(removed);
			return true;
		}
		return false;
	}

	@Override
	public synchronized Collection<SealedEnvelope> getAll() {
		return Collections.unmodifiableCollection(new ArrayList<>(store.values()));
	}

	@Override
	public synchronized int getCount() {
		return store.size();
	}

	@Override
	public synchronized long getByteSize() {
		return currentByteSize;
	}

	@Override
	public synchronized void pruneExpired(long currentTimestampSeconds) {
		Iterator<Map.Entry<EnvelopeId, SealedEnvelope>> it =
				store.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<EnvelopeId, SealedEnvelope> entry = it.next();
			if (entry.getValue().getExpiry() <= currentTimestampSeconds) {
				currentByteSize -= envelopeSize(entry.getValue());
				it.remove();
			}
		}
	}

	@Override
	public synchronized void evictToCapacity(long maxBytes) {
		while (currentByteSize > maxBytes && !store.isEmpty()) {
			// Find envelope with lowest copies; tie break on oldest (first in LinkedHashMap)
			EnvelopeId candidateId = null;
			int lowestCopies = Integer.MAX_VALUE;

			for (SealedEnvelope env : store.values()) {
				if (env.getCopies() < lowestCopies) {
					lowestCopies = env.getCopies();
					candidateId = env.getId();
					if (lowestCopies == 1) break; // lowest possible
				}
			}

			if (candidateId != null) {
				remove(candidateId);
			} else {
				// Fallback: evict oldest entry
				EnvelopeId first = store.keySet().iterator().next();
				remove(first);
			}
		}
	}
}
