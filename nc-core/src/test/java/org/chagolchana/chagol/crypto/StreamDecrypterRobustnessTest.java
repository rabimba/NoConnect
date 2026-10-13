package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import static org.chagolchana.chagol.api.transport.TransportConstants.MAX_PAYLOAD_LENGTH;
import static org.junit.Assert.fail;

/**
 * Feeds random ciphertext to the stream decrypter, which processes data from
 * untrusted peers. Decryption must either succeed or throw an IOException
 * (FormatException for authentication failures, EOFException for truncated
 * input): never another exception. Seeded so failures reproduce.
 */
public class StreamDecrypterRobustnessTest extends ChagolTestCase {

	private static final int ITERATIONS = 20_000;

	@Test
	public void testRandomCiphertext() {
		Random random = new Random(0x4e43_0005L);
		byte[] payload = new byte[MAX_PAYLOAD_LENGTH];
		for (int i = 0; i < ITERATIONS; i++) {
			byte[] keyBytes = new byte[SecretKey.LENGTH];
			random.nextBytes(keyBytes);
			byte[] buf = new byte[random.nextInt(256)];
			random.nextBytes(buf);
			StreamDecrypterImpl s = new StreamDecrypterImpl(
					new ByteArrayInputStream(buf),
					new XSalsa20Poly1305AuthenticatedCipher(),
					random.nextInt(1000), new SecretKey(keyBytes));
			try {
				while (s.readFrame(payload) != -1) {
					// Keep reading until end of stream or rejection
				}
			} catch (IOException expected) {
				// Truncated or unauthenticated input is rejected
			} catch (Throwable t) {
				fail("Iteration " + i + " threw " + t);
			}
		}
	}
}
