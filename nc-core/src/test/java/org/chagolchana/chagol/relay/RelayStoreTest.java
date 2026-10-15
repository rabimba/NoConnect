package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RelayStoreTest extends ChagolTestCase {

	private final byte[] tag = new byte[16];
	private RelayStore store;

	@Before
	public void setUp() {
		store = new MemoryRelayStore(10 * 1024); // 10 KB limit for test
	}

	@Test
	public void testPutGetRemove() {
		SealedEnvelope env = SealedEnvelope.create(tag, 1_000_000L, 8, 8,
				new byte[100]);
		store.put(env);

		assertTrue(store.has(env.getId()));
		assertEquals(1, store.getCount());
		assertTrue(store.getByteSize() > 1024);

		SealedEnvelope retrieved = store.get(env.getId());
		assertNotNull(retrieved);
		assertEquals(env.getId(), retrieved.getId());

		assertTrue(store.remove(env.getId()));
		assertFalse(store.has(env.getId()));
		assertEquals(0, store.getCount());
		assertEquals(0L, store.getByteSize());
	}

	@Test
	public void testPruneExpired() {
		SealedEnvelope expired = SealedEnvelope.create(tag, 1000L, 8, 8,
				new byte[50]);
		SealedEnvelope valid = SealedEnvelope.create(tag, 2000L, 8, 8,
				new byte[50]);

		store.put(expired);
		store.put(valid);
		assertEquals(2, store.getCount());

		store.pruneExpired(1500L); // timestamp after expired, before valid

		assertEquals(1, store.getCount());
		assertFalse(store.has(expired.getId()));
		assertTrue(store.has(valid.getId()));
	}

	@Test
	public void testEvictionEnforcesCapacity() {
		// Store with 2.5 KB limit (fits ~2 1KB envelopes)
		RelayStore boundedStore = new MemoryRelayStore(2500);

		// Env 1: 1 copy (Wait mode)
		byte[] tag1 = new byte[16];
		tag1[0] = 1;
		SealedEnvelope env1 = SealedEnvelope.create(tag1, 3000L, 8, 1,
				new byte[50]);

		// Env 2: 4 copies
		byte[] tag2 = new byte[16];
		tag2[0] = 2;
		SealedEnvelope env2 = SealedEnvelope.create(tag2, 3000L, 8, 4,
				new byte[50]);

		// Env 3: 8 copies
		byte[] tag3 = new byte[16];
		tag3[0] = 3;
		SealedEnvelope env3 = SealedEnvelope.create(tag3, 3000L, 8, 8,
				new byte[50]);

		boundedStore.put(env1);
		boundedStore.put(env2);
		assertEquals(2, boundedStore.getCount());

		// Adding 3rd should trigger eviction of env1 (lowest copies)
		boundedStore.put(env3);

		assertTrue(boundedStore.getByteSize() <= 2500);
		assertEquals(2, boundedStore.getCount());
		assertFalse(boundedStore.has(env1.getId()));
		assertTrue(boundedStore.has(env2.getId()));
		assertTrue(boundedStore.has(env3.getId()));
	}

	@Test
	public void testDuplicateEnvelopeMergesCopies() {
		SealedEnvelope env1 = SealedEnvelope.create(tag, 1000L, 8, 2,
				new byte[50]);
		store.put(env1);
		assertEquals(2, store.get(env1.getId()).getCopies());

		// Put same envelope ID with 6 copies
		SealedEnvelope envHigherCopies = new SealedEnvelope(env1.getId(),
				env1.getRecipientTag(), env1.getExpiry(), env1.getHopLimit(),
				6, env1.getPayloadLength(), env1.getPaddedCiphertext());
		store.put(envHigherCopies);

		assertEquals(1, store.getCount());
		assertEquals(6, store.get(env1.getId()).getCopies());
	}
}
