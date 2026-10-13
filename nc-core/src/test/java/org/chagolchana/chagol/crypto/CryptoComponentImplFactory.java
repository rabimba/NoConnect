package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.test.TestSecureRandomProvider;

/**
 * Gives tests in other packages access to the package-private
 * {@link CryptoComponentImpl}, without password-based encryption.
 */
public class CryptoComponentImplFactory {

	public static CryptoComponent create() {
		return new CryptoComponentImpl(new TestSecureRandomProvider(), null);
	}
}
