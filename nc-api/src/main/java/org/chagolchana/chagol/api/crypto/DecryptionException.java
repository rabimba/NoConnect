package org.chagolchana.chagol.api.crypto;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public class DecryptionException extends Exception {

	private final DecryptionResult result;

	public DecryptionException(DecryptionResult result) {
		this.result = result;
	}

	public DecryptionResult getDecryptionResult() {
		return result;
	}
}
