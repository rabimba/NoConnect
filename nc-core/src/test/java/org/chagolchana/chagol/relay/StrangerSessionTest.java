package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.crypto.CryptoComponentImplFactory;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StrangerSessionTest extends ChagolTestCase {

	private CryptoComponent crypto;
	private RelayStore storeAlice;
	private RelayStore storeBob;

	@Before
	public void setUp() {
		crypto = CryptoComponentImplFactory.create();
		storeAlice = new MemoryRelayStore(1024 * 1024);
		storeBob = new MemoryRelayStore(1024 * 1024);
	}

	private SealedEnvelope createEnvelope(int idByte, long expiry) {
		byte[] tag = new byte[16];
		tag[0] = (byte) (idByte & 0xFF);
		byte[] payload = ("Message payload " + idByte).getBytes();
		return SealedEnvelope.create(tag, expiry, 8, 8, payload);
	}

	@Test
	public void testStrangerSessionExchangesMissingEnvelopes() throws Exception {
		// Alice has env 1, 2, 3
		SealedEnvelope env1 = createEnvelope(1, 2_000_000L);
		SealedEnvelope env2 = createEnvelope(2, 2_000_000L);
		SealedEnvelope env3 = createEnvelope(3, 2_000_000L);
		storeAlice.put(env1);
		storeAlice.put(env2);
		storeAlice.put(env3);

		// Bob has env 3, 4, 5
		SealedEnvelope env4 = createEnvelope(4, 2_000_000L);
		SealedEnvelope env5 = createEnvelope(5, 2_000_000L);
		storeBob.put(env3);
		storeBob.put(env4);
		storeBob.put(env5);

		assertEquals(3, storeAlice.getCount());
		assertEquals(3, storeBob.getCount());

		// Pipe streams between Alice and Bob
		PipedOutputStream aliceOut = new PipedOutputStream();
		PipedInputStream bobIn = new PipedInputStream(aliceOut);

		PipedOutputStream bobOut = new PipedOutputStream();
		PipedInputStream aliceIn = new PipedInputStream(bobOut);

		StrangerSession sessionAlice = new StrangerSession(crypto, storeAlice,
				aliceIn, aliceOut, true);
		StrangerSession sessionBob = new StrangerSession(crypto, storeBob,
				bobIn, bobOut, false);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch latch = new CountDownLatch(2);

		executor.execute(() -> {
			try {
				sessionAlice.run();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});

		executor.execute(() -> {
			try {
				sessionBob.run();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});

		boolean completed = latch.await(5, TimeUnit.SECONDS);
		executor.shutdown();
		assertTrue("Stranger session did not complete in time", completed);

		// Both Alice and Bob should now have all 5 envelopes!
		assertEquals(5, storeAlice.getCount());
		assertEquals(5, storeBob.getCount());

		assertTrue(storeAlice.has(env1.getId()));
		assertTrue(storeAlice.has(env2.getId()));
		assertTrue(storeAlice.has(env3.getId()));
		assertTrue(storeAlice.has(env4.getId()));
		assertTrue(storeAlice.has(env5.getId()));

		assertTrue(storeBob.has(env1.getId()));
		assertTrue(storeBob.has(env2.getId()));
		assertTrue(storeBob.has(env3.getId()));
		assertTrue(storeBob.has(env4.getId()));
		assertTrue(storeBob.has(env5.getId()));
	}
}
