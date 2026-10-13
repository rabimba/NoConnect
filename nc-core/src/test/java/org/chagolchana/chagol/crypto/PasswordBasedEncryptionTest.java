package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.DecryptionException;
import org.chagolchana.chagol.api.crypto.KeyStrengthener;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.system.SystemClock;
import org.chagolchana.chagol.test.ChagolMockTestCase;
import org.chagolchana.chagol.test.TestSecureRandomProvider;
import org.jmock.Expectations;
import org.junit.Test;

import static org.chagolchana.chagol.api.crypto.DecryptionResult.INVALID_CIPHERTEXT;
import static org.chagolchana.chagol.api.crypto.DecryptionResult.INVALID_PASSWORD;
import static org.chagolchana.chagol.api.crypto.DecryptionResult.KEY_STRENGTHENER_ERROR;
import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.util.ByteUtils.writeUint32;
import static org.chagolchana.chagol.test.TestUtils.getSecretKey;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PasswordBasedEncryptionTest extends ChagolMockTestCase {

	private final KeyStrengthener keyStrengthener =
			context.mock(KeyStrengthener.class);

	// Use the smallest Argon2id memory size so the tests stay fast
	private final Argon2idKdf argon2 = new Argon2idKdf(new SystemClock()) {
		@Override
		public int chooseCostParameter() {
			return Argon2idKdf.MIN_DECRYPT_COST;
		}
	};

	private final CryptoComponentImpl crypto =
			new CryptoComponentImpl(new TestSecureRandomProvider(), argon2,
					new ScryptKdf(new SystemClock()));

	@Test
	public void testEncryptionAndDecryption() throws Exception {
		byte[] input = getRandomBytes(1234);
		String password = "password";
		byte[] ciphertext = crypto.encryptWithPassword(input, password, null);
		byte[] output = crypto.decryptWithPassword(ciphertext, password, null);
		assertArrayEquals(input, output);
	}

	@Test
	public void testInvalidFormatVersionThrowsException() {
		byte[] input = getRandomBytes(1234);
		String password = "password";
		byte[] ciphertext = crypto.encryptWithPassword(input, password, null);

		// Modify the format version
		ciphertext[0] ^= (byte) 0xFF;
		try {
			crypto.decryptWithPassword(ciphertext, password, null);
			fail();
		} catch (DecryptionException expected) {
			assertEquals(INVALID_CIPHERTEXT, expected.getDecryptionResult());
		}
	}

	@Test
	public void testInvalidPasswordThrowsException() {
		byte[] input = getRandomBytes(1234);
		byte[] ciphertext = crypto.encryptWithPassword(input, "password", null);

		// Try to decrypt with the wrong password
		try {
			crypto.decryptWithPassword(ciphertext, "wrong", null);
			fail();
		} catch (DecryptionException expected) {
			assertEquals(INVALID_PASSWORD, expected.getDecryptionResult());
		}
	}

	@Test
	public void testMissingKeyStrengthenerThrowsException() {
		SecretKey strengthened = getSecretKey();
		context.checking(new Expectations() {{
			oneOf(keyStrengthener).strengthenKey(with(any(SecretKey.class)));
			will(returnValue(strengthened));
		}});

		// Use the key strengthener during encryption
		byte[] input = getRandomBytes(1234);
		String password = "password";
		byte[] ciphertext =
				crypto.encryptWithPassword(input, password, keyStrengthener);

		// The key strengthener is missing during decryption
		try {
			crypto.decryptWithPassword(ciphertext, password, null);
			fail();
		} catch (DecryptionException expected) {
			assertEquals(KEY_STRENGTHENER_ERROR, expected.getDecryptionResult());
		}
	}

	@Test
	public void testKeyStrengthenerFailureThrowsException() {
		SecretKey strengthened = getSecretKey();
		context.checking(new Expectations() {{
			oneOf(keyStrengthener).strengthenKey(with(any(SecretKey.class)));
			will(returnValue(strengthened));
			oneOf(keyStrengthener).isInitialised();
			will(returnValue(false));
		}});

		// Use the key strengthener during encryption
		byte[] input = getRandomBytes(1234);
		String password = "password";
		byte[] ciphertext =
				crypto.encryptWithPassword(input, password, keyStrengthener);

		// The key strengthener fails during decryption
		try {
			crypto.decryptWithPassword(ciphertext, password, keyStrengthener);
			fail();
		} catch (DecryptionException expected) {
			assertEquals(KEY_STRENGTHENER_ERROR, expected.getDecryptionResult());
		}
	}

	@Test
	public void testNewCiphertextUsesArgon2id() {
		byte[] ciphertext =
				crypto.encryptWithPassword(getRandomBytes(32), "password", null);
		assertEquals(2, ciphertext[0]);
		assertFalse(crypto.isEncryptedWithLegacyKdf(ciphertext));
		assertFalse(crypto.isEncryptedWithStrengthenedKey(ciphertext));
	}

	@Test
	public void testStrengthenedCiphertextUsesArgon2id() {
		SecretKey strengthened = getSecretKey();
		context.checking(new Expectations() {{
			oneOf(keyStrengthener).strengthenKey(with(any(SecretKey.class)));
			will(returnValue(strengthened));
		}});
		byte[] ciphertext = crypto.encryptWithPassword(getRandomBytes(32),
				"password", keyStrengthener);
		assertEquals(3, ciphertext[0]);
		assertFalse(crypto.isEncryptedWithLegacyKdf(ciphertext));
		assertTrue(crypto.isEncryptedWithStrengthenedKey(ciphertext));
	}

	@Test
	public void testLegacyScryptCiphertextCanBeDecrypted() throws Exception {
		byte[] input = getRandomBytes(1234);
		String password = "password";
		byte[] ciphertext =
				crypto.encryptWithPassword(input, password, null, true);
		assertEquals(0, ciphertext[0]);
		assertTrue(crypto.isEncryptedWithLegacyKdf(ciphertext));
		byte[] output = crypto.decryptWithPassword(ciphertext, password, null);
		assertArrayEquals(input, output);
	}

	@Test
	public void testOversizedArgon2idCostIsRejected() {
		byte[] ciphertext =
				crypto.encryptWithPassword(getRandomBytes(32), "password", null);
		writeUint32(Argon2idKdf.MAX_COST + 1L, ciphertext, COST_OFFSET);
		assertInvalidCiphertext(ciphertext);
	}

	@Test
	public void testUndersizedArgon2idCostIsRejected() {
		byte[] ciphertext =
				crypto.encryptWithPassword(getRandomBytes(32), "password", null);
		writeUint32(Argon2idKdf.MIN_DECRYPT_COST - 1L, ciphertext, COST_OFFSET);
		assertInvalidCiphertext(ciphertext);
	}

	@Test
	public void testHugeLegacyCostIsRejected() {
		byte[] ciphertext = crypto.encryptWithPassword(getRandomBytes(32),
				"password", null, true);
		// Would need terabytes of memory if scrypt tried to use it
		writeUint32(1L << 30, ciphertext, COST_OFFSET);
		assertInvalidCiphertext(ciphertext);
	}

	@Test
	public void testNonPowerOfTwoLegacyCostIsRejected() {
		byte[] ciphertext = crypto.encryptWithPassword(getRandomBytes(32),
				"password", null, true);
		writeUint32(1000, ciphertext, COST_OFFSET);
		assertInvalidCiphertext(ciphertext);
	}

	// Format version (1 byte) followed by the salt
	private static final int COST_OFFSET = 1 + 32;

	private void assertInvalidCiphertext(byte[] ciphertext) {
		try {
			crypto.decryptWithPassword(ciphertext, "password", null);
			fail();
		} catch (DecryptionException expected) {
			assertEquals(INVALID_CIPHERTEXT, expected.getDecryptionResult());
		}
	}
}
