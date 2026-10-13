package org.chagolchana.chagol.transport.agreement;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.crypto.KeyPair;
import org.chagolchana.chagol.api.crypto.PrivateKey;
import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

@NotNullByDefault
interface TransportKeyAgreementCrypto {

	KeyPair generateKeyPair();

	SecretKey deriveRootKey(KeyPair localKeyPair, PublicKey remotePublicKey)
			throws GeneralSecurityException;

	PublicKey parsePublicKey(byte[] encoded) throws FormatException;

	PrivateKey parsePrivateKey(byte[] encoded) throws FormatException;
}
