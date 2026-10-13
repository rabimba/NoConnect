package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.chagolchana.chagol.crypto.HybridKem.CIPHERTEXT_BYTES;
import static org.chagolchana.chagol.crypto.HybridKem.PUBLIC_KEY_BYTES;
import static org.chagolchana.chagol.crypto.HybridKem.SHARED_SECRET_BYTES;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class HybridKemTest extends ChagolTestCase {

	private final HybridKem kem = new HybridKem(new SecureRandom());

	@Test
	public void testRoundTrip() throws Exception {
		HybridKem.KeyPair kp = kem.generateKeyPair();
		assertEquals(PUBLIC_KEY_BYTES, kp.getPublicKey().length);
		HybridKem.Encapsulation enc = kem.encapsulate(kp.getPublicKey());
		assertEquals(CIPHERTEXT_BYTES, enc.getCiphertext().length);
		assertEquals(SHARED_SECRET_BYTES, enc.getSharedSecret().length);
		assertArrayEquals(enc.getSharedSecret(),
				kem.decapsulate(kp, enc.getCiphertext()));
	}

	@Test
	public void testFreshSecretEachEncapsulation() throws Exception {
		HybridKem.KeyPair kp = kem.generateKeyPair();
		byte[] a = kem.encapsulate(kp.getPublicKey()).getSharedSecret();
		byte[] b = kem.encapsulate(kp.getPublicKey()).getSharedSecret();
		assertFalse(Arrays.equals(a, b));
	}

	@Test
	public void testTamperingWithEitherHalfChangesTheSecret()
			throws Exception {
		HybridKem.KeyPair kp = kem.generateKeyPair();
		HybridKem.Encapsulation enc = kem.encapsulate(kp.getPublicKey());
		byte[] ct = enc.getCiphertext();
		// ML-KEM part
		byte[] badM = ct.clone();
		badM[0] ^= 1;
		assertFalse(Arrays.equals(enc.getSharedSecret(),
				kem.decapsulate(kp, badM)));
		// X25519 part
		byte[] badX = ct.clone();
		badX[ct.length - 1] ^= 1;
		assertFalse(Arrays.equals(enc.getSharedSecret(),
				kem.decapsulate(kp, badX)));
	}

	@Test
	public void testWrongKeyPairGetsDifferentSecret() throws Exception {
		HybridKem.KeyPair kp = kem.generateKeyPair();
		HybridKem.Encapsulation enc = kem.encapsulate(kp.getPublicKey());
		assertFalse(Arrays.equals(enc.getSharedSecret(),
				kem.decapsulate(kem.generateKeyPair(), enc.getCiphertext())));
	}

	@Test
	public void testWrongLengthsAreRejected() throws Exception {
		HybridKem.KeyPair kp = kem.generateKeyPair();
		try {
			kem.encapsulate(new byte[PUBLIC_KEY_BYTES - 1]);
			fail();
		} catch (GeneralSecurityException expected) {
			// Expected
		}
		try {
			kem.decapsulate(kp, new byte[CIPHERTEXT_BYTES + 1]);
			fail();
		} catch (GeneralSecurityException expected) {
			// Expected
		}
	}
}
