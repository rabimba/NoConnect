package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NegentropyReconcilerTest extends ChagolTestCase {

	private SealedEnvelope createEnvelope(int index, long expiry) {
		byte[] tag = new byte[16];
		tag[0] = (byte) (index & 0xFF);
		byte[] payload = ("Payload " + index).getBytes();
		return SealedEnvelope.create(tag, expiry, 8, 8, payload);
	}

	@Test
	public void testIdenticalSetsReconcileWithZeroDiffs() {
		List<SealedEnvelope> items = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			items.add(createEnvelope(i, 1_000_000L + i));
		}

		NegentropyReconciler alice = new NegentropyReconciler(items);
		NegentropyReconciler bob = new NegentropyReconciler(items);

		// Alice creates initial query message
		byte[] query = alice.initiateQuery();

		// Bob processes query and produces response
		NegentropyReconciler.ReconciliationResult bobResult = bob.processMessage(query);
		assertTrue(bobResult.hasResponseMessage());

		// Alice processes Bob's response
		NegentropyReconciler.ReconciliationResult aliceResult = alice.processMessage(bobResult.getResponseMessage());

		// Both sides should detect 0 items to send or receive
		assertEquals(0, aliceResult.getNeedIds().size());
		assertEquals(0, aliceResult.getSendIds().size());

		assertEquals(0, bobResult.getNeedIds().size());
		assertEquals(0, bobResult.getSendIds().size());
	}

	@Test
	public void testDisjointSetsIdentifyDifferences() {
		List<SealedEnvelope> shared = new ArrayList<>();
		for (int i = 10; i < 20; i++) {
			shared.add(createEnvelope(i, 1_000_000L + i));
		}

		List<SealedEnvelope> aliceOnly = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			aliceOnly.add(createEnvelope(i, 1_000_000L + i));
		}

		List<SealedEnvelope> bobOnly = new ArrayList<>();
		for (int i = 25; i < 30; i++) {
			bobOnly.add(createEnvelope(i, 1_000_000L + i));
		}

		List<SealedEnvelope> aliceItems = new ArrayList<>(shared);
		aliceItems.addAll(aliceOnly);

		List<SealedEnvelope> bobItems = new ArrayList<>(shared);
		bobItems.addAll(bobOnly);

		NegentropyReconciler alice = new NegentropyReconciler(aliceItems);
		NegentropyReconciler bob = new NegentropyReconciler(bobItems);

		byte[] msg = alice.initiateQuery();

		// Round 1
		NegentropyReconciler.ReconciliationResult bobResult = bob.processMessage(msg);
		assertTrue(bobResult.hasResponseMessage());

		// Round 2
		NegentropyReconciler.ReconciliationResult aliceResult = alice.processMessage(bobResult.getResponseMessage());

		// Bob needs aliceOnly (5 items), Bob provides bobOnly (5 items)
		for (SealedEnvelope env : bobOnly) {
			assertTrue(aliceResult.getNeedIds().contains(env.getId()));
		}
		for (SealedEnvelope env : aliceOnly) {
			assertTrue(aliceResult.getSendIds().contains(env.getId()));
		}
	}

	@Test
	public void testEmptyAgainstPopulated() {
		List<SealedEnvelope> items = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			items.add(createEnvelope(i, 1_000_000L + i));
		}

		NegentropyReconciler emptyAlice = new NegentropyReconciler(new ArrayList<>());
		NegentropyReconciler populatedBob = new NegentropyReconciler(items);

		byte[] query = emptyAlice.initiateQuery();
		NegentropyReconciler.ReconciliationResult bobResult = populatedBob.processMessage(query);

		NegentropyReconciler.ReconciliationResult aliceResult = emptyAlice.processMessage(bobResult.getResponseMessage());

		// Alice has nothing, needs all 10
		assertEquals(10, aliceResult.getNeedIds().size());
		assertEquals(0, aliceResult.getSendIds().size());

		// Bob has all 10, needs to send all 10
		assertEquals(10, bobResult.getSendIds().size());
		assertEquals(0, bobResult.getNeedIds().size());
	}
}
