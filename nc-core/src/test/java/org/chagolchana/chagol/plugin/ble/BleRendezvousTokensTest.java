package org.chagolchana.chagol.plugin.ble;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.crypto.CryptoComponentImplFactory;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.EPOCH_MS;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.TOKEN_BYTES;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.epochFor;
import static org.chagolchana.chagol.test.TestUtils.getSecretKey;
import static org.chagolchana.chagol.util.StringUtils.toHexString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BleRendezvousTokensTest extends ChagolTestCase {

	private final BleRendezvousTokens tokens =
			new BleRendezvousTokens(CryptoComponentImplFactory.create());

	@Test
	public void testBothSidesDeriveTheSameToken() {
		SecretKey k = getSecretKey();
		byte[] a = tokens.deriveToken(k, 1000);
		byte[] b = tokens.deriveToken(new SecretKey(k.getBytes()), 1000);
		assertEquals(TOKEN_BYTES, a.length);
		assertArrayEquals(a, b);
	}

	@Test
	public void testTokensRotateEveryEpoch() {
		SecretKey k = getSecretKey();
		Set<String> seen = new HashSet<>();
		for (long epoch = 0; epoch < 1000; epoch++) {
			seen.add(toHexString(tokens.deriveToken(k, epoch)));
		}
		// 64-bit tokens: a collision among 1000 would be astronomically rare
		assertEquals(1000, seen.size());
	}

	@Test
	public void testDifferentContactsGetDifferentTokens() {
		byte[] a = tokens.deriveToken(getSecretKey(), 42);
		byte[] b = tokens.deriveToken(getSecretKey(), 42);
		assertEquals(false, java.util.Arrays.equals(a, b));
	}

	@Test
	public void testMatchAllowsOneEpochOfClockDifference() {
		SecretKey alice = getSecretKey(), bob = getSecretKey();
		Map<String, SecretKey> contacts = new HashMap<>();
		contacts.put("alice", alice);
		contacts.put("bob", bob);
		long now = 1_700_000_000_000L;
		long epoch = epochFor(now);

		assertEquals("bob", tokens.match(tokens.deriveToken(bob, epoch),
				contacts, now));
		assertEquals("bob", tokens.match(tokens.deriveToken(bob, epoch - 1),
				contacts, now));
		assertEquals("alice", tokens.match(
				tokens.deriveToken(alice, epoch + 1), contacts, now));
		assertNull(tokens.match(tokens.deriveToken(bob, epoch + 2),
				contacts, now));
		assertNull(tokens.match(tokens.deriveToken(getSecretKey(), epoch),
				contacts, now));
		assertNull(tokens.match(new byte[TOKEN_BYTES - 1], contacts, now));
	}

	@Test
	public void testEpochBoundaries() {
		assertEquals(0, epochFor(0));
		assertEquals(0, epochFor(EPOCH_MS - 1));
		assertEquals(1, epochFor(EPOCH_MS));
		assertEquals(-1, epochFor(-1));
	}
}
