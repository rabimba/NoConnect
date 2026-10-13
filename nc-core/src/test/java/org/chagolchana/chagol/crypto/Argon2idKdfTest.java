package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.system.SystemClock;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.util.StringUtils.fromHexString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Argon2idKdfTest extends ChagolTestCase {

	private static final int COST = Argon2idKdf.MIN_DECRYPT_COST;

	private final Argon2idKdf kdf = new Argon2idKdf(new SystemClock());

	/**
	 * Known answer computed with the reference C implementation (libargon2
	 * via argon2-cffi): Argon2id v1.3, t = 3, m = 8192 KiB, p = 1, 32-byte
	 * output, password "password", salt 0x00..0x1f.
	 */
	@Test
	public void testKnownAnswer() throws Exception {
		byte[] salt = new byte[32];
		for (int i = 0; i < salt.length; i++) salt[i] = (byte) i;
		SecretKey k = kdf.deriveKey("password", salt, 8192);
		assertArrayEquals(fromHexString(
				"d32fb2c06eff4c38ff348039c85c7bb88bc5721cd2a970f03bcb8d42f1fb3026"),
				k.getBytes());
	}

	@Test
	public void testSameInputsGiveSameKey() {
		byte[] salt = getRandomBytes(32);
		SecretKey a = kdf.deriveKey("password", salt, COST);
		SecretKey b = kdf.deriveKey("password", salt, COST);
		assertArrayEquals(a.getBytes(), b.getBytes());
	}

	@Test
	public void testDifferentSaltGivesDifferentKey() {
		SecretKey a = kdf.deriveKey("password", getRandomBytes(32), COST);
		SecretKey b = kdf.deriveKey("password", getRandomBytes(32), COST);
		assertFalse(java.util.Arrays.equals(a.getBytes(), b.getBytes()));
	}

	@Test
	public void testDifferentPasswordGivesDifferentKey() {
		byte[] salt = getRandomBytes(32);
		SecretKey a = kdf.deriveKey("password", salt, COST);
		SecretKey b = kdf.deriveKey("passwore", salt, COST);
		assertFalse(java.util.Arrays.equals(a.getBytes(), b.getBytes()));
	}

	@Test
	public void testDifferentCostGivesDifferentKey() {
		byte[] salt = getRandomBytes(32);
		SecretKey a = kdf.deriveKey("password", salt, COST);
		SecretKey b = kdf.deriveKey("password", salt, COST * 2);
		assertFalse(java.util.Arrays.equals(a.getBytes(), b.getBytes()));
	}

	@Test
	public void testCalibratedCostIsWithinBounds() {
		int cost = kdf.chooseCostParameter();
		assertTrue(Argon2idKdf.isValidCost(cost));
		long quarterHeapKiB = Runtime.getRuntime().maxMemory() / 4 / 1024;
		assertTrue(cost <= Math.max(Argon2idKdf.MIN_DECRYPT_COST,
				quarterHeapKiB));
	}

	@Test
	public void testCostBounds() {
		assertFalse(Argon2idKdf.isValidCost(Argon2idKdf.MIN_DECRYPT_COST - 1));
		assertTrue(Argon2idKdf.isValidCost(Argon2idKdf.MIN_DECRYPT_COST));
		assertTrue(Argon2idKdf.isValidCost(Argon2idKdf.MAX_COST));
		assertFalse(Argon2idKdf.isValidCost(Argon2idKdf.MAX_COST + 1L));
	}
}
