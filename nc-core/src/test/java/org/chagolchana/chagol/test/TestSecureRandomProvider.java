package org.chagolchana.chagol.test;

import org.chagolchana.chagol.api.system.SecureRandomProvider;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.security.Provider;

@NotNullByDefault
public class TestSecureRandomProvider implements SecureRandomProvider {

	@Override
	public Provider getProvider() {
		// Use the default provider
		return null;
	}
}
