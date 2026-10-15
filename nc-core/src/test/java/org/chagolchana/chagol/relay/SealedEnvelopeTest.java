package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SealedEnvelopeTest extends ChagolTestCase {

	private final byte[] recipientTag = new byte[] {
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
	};
	private final long expiry = 1_800_000_000L;

	@Test
	public void testCreateAndEncodeDecodeRoundTripSmallTier() throws Exception {
		byte[] payload = "Hello Offgrid Mesh!".getBytes("UTF-8");
		SealedEnvelope env = SealedEnvelope.create(recipientTag, expiry, 8, 8,
				payload);

		assertEquals(1024, env.getPaddedCiphertext().length);
		assertEquals(payload.length, env.getPayloadLength());
		assertArrayEquals(payload, env.getPayload());
		assertEquals(8, env.getHopLimit());
		assertEquals(8, env.getCopies());

		byte[] wire = env.encode();
		assertEquals(1 + 16 + 4 + 1 + 1 + 2 + 1024, wire.length);

		SealedEnvelope decoded = SealedEnvelope.decode(wire);
		assertArrayEquals(env.getId().getBytes(), decoded.getId().getBytes());
		assertArrayEquals(recipientTag, decoded.getRecipientTag());
		assertEquals(expiry, decoded.getExpiry());
		assertEquals(8, decoded.getHopLimit());
		assertEquals(8, decoded.getCopies());
		assertEquals(payload.length, decoded.getPayloadLength());
		assertArrayEquals(payload, decoded.getPayload());
	}

	@Test
	public void testPaddingTiers() throws Exception {
		// Tier 1: <= 1024
		byte[] small = new byte[100];
		SealedEnvelope env1 = SealedEnvelope.create(recipientTag, expiry, 8, 8,
				small);
		assertEquals(1024, env1.getPaddedCiphertext().length);

		// Tier 2: 1025 .. 4096
		byte[] medium = new byte[1500];
		SealedEnvelope env2 = SealedEnvelope.create(recipientTag, expiry, 8, 8,
				medium);
		assertEquals(4096, env2.getPaddedCiphertext().length);

		// Tier 3: 4097 .. 16384
		byte[] large = new byte[5000];
		SealedEnvelope env3 = SealedEnvelope.create(recipientTag, expiry, 8, 8,
				large);
		assertEquals(16384, env3.getPaddedCiphertext().length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPayloadExceedsMaxTier() {
		byte[] tooLarge = new byte[16385];
		SealedEnvelope.create(recipientTag, expiry, 8, 8, tooLarge);
	}

	@Test(expected = FormatException.class)
	public void testDecodeInvalidVersion() throws Exception {
		byte[] wire = SealedEnvelope.create(recipientTag, expiry, 8, 8,
				new byte[10]).encode();
		wire[0] = 0x02; // unsupported version
		SealedEnvelope.decode(wire);
	}

	@Test(expected = FormatException.class)
	public void testDecodeTruncated() throws Exception {
		byte[] wire = SealedEnvelope.create(recipientTag, expiry, 8, 8,
				new byte[10]).encode();
		SealedEnvelope.decode(Arrays.copyOf(wire, wire.length - 10));
	}

	@Test
	public void testSprayAndWaitCopySplit() {
		SealedEnvelope original = SealedEnvelope.create(recipientTag, expiry, 8,
				8, new byte[20]);
		// Node A splits: original keeps 4, child gets 4
		SealedEnvelope child = original.splitCopies();
		assertEquals(4, original.getCopies());
		assertEquals(4, child.getCopies());
		// Envelope IDs remain identical across splits!
		assertArrayEquals(original.getId().getBytes(),
				child.getId().getBytes());

		// Split again: 4 -> 2 and 2
		SealedEnvelope child2 = child.splitCopies();
		assertEquals(2, child.getCopies());
		assertEquals(2, child2.getCopies());

		// Split 3 -> 1 and 2
		SealedEnvelope odd = SealedEnvelope.create(recipientTag, expiry, 8, 3,
				new byte[20]);
		SealedEnvelope oddChild = odd.splitCopies();
		assertEquals(1, odd.getCopies());
		assertEquals(2, oddChild.getCopies());

		// Cannot split when copies == 1 (wait mode)
		SealedEnvelope last = SealedEnvelope.create(recipientTag, expiry, 8, 1,
				new byte[20]);
		assertFalse(last.canSplit());
	}
}
