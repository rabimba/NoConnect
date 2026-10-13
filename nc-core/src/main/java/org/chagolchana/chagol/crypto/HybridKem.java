package org.chagolchana.chagol.crypto;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMExtractor;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import static java.lang.System.arraycopy;
import static org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters.ml_kem_768;

/**
 * EXPERIMENTAL, not used by any protocol yet (gated by
 * FeatureFlags.shouldEnableHybridPqKeyAgreement()). A hybrid KEM combining
 * X25519 with ML-KEM-768 (FIPS 203), with the shared secret derived by the
 * X-Wing combiner (draft-connolly-cfrg-xwing-kem):
 * <pre>
 * ss = SHA3-256(ss_M || ss_X || ct_X || pk_X || label)
 * </pre>
 * The result stays secret as long as either X25519 or ML-KEM is secure, which
 * protects contacts added today against "harvest now, decrypt later".
 * Key generation differs from X-Wing's seed-based scheme, so this has not been
 * checked against X-Wing's test vectors.
 */
@NotNullByDefault
public class HybridKem {

	static final int X25519_KEY_BYTES = 32;
	public static final int PUBLIC_KEY_BYTES = 1184 + X25519_KEY_BYTES;
	public static final int CIPHERTEXT_BYTES = 1088 + X25519_KEY_BYTES;
	public static final int SHARED_SECRET_BYTES = 32;

	// X-Wing label: the ASCII bytes \.//^\
	private static final byte[] LABEL =
			{0x5c, 0x2e, 0x2f, 0x2f, 0x5e, 0x5c};

	private final SecureRandom random;

	public HybridKem(SecureRandom random) {
		this.random = random;
	}

	/**
	 * A key pair: the public key is ML-KEM public key || X25519 public key.
	 */
	public static class KeyPair {

		final MLKEMPrivateKeyParameters mlkemPrivate;
		final X25519PrivateKeyParameters x25519Private;
		final byte[] publicKey;

		KeyPair(MLKEMPrivateKeyParameters mlkemPrivate,
				X25519PrivateKeyParameters x25519Private, byte[] publicKey) {
			this.mlkemPrivate = mlkemPrivate;
			this.x25519Private = x25519Private;
			this.publicKey = publicKey;
		}

		public byte[] getPublicKey() {
			return publicKey.clone();
		}
	}

	public static class Encapsulation {

		private final byte[] ciphertext, sharedSecret;

		Encapsulation(byte[] ciphertext, byte[] sharedSecret) {
			this.ciphertext = ciphertext;
			this.sharedSecret = sharedSecret;
		}

		public byte[] getCiphertext() {
			return ciphertext.clone();
		}

		public byte[] getSharedSecret() {
			return sharedSecret.clone();
		}
	}

	public KeyPair generateKeyPair() {
		MLKEMKeyPairGenerator mlkemGen = new MLKEMKeyPairGenerator();
		mlkemGen.init(new MLKEMKeyGenerationParameters(random, ml_kem_768));
		AsymmetricCipherKeyPair mlkem = mlkemGen.generateKeyPair();
		X25519KeyPairGenerator xGen = new X25519KeyPairGenerator();
		xGen.init(new X25519KeyGenerationParameters(random));
		AsymmetricCipherKeyPair x = xGen.generateKeyPair();
		byte[] pkM = ((MLKEMPublicKeyParameters) mlkem.getPublic())
				.getEncoded();
		byte[] pkX = ((X25519PublicKeyParameters) x.getPublic()).getEncoded();
		return new KeyPair((MLKEMPrivateKeyParameters) mlkem.getPrivate(),
				(X25519PrivateKeyParameters) x.getPrivate(), concat(pkM, pkX));
	}

	public Encapsulation encapsulate(byte[] publicKey)
			throws GeneralSecurityException {
		if (publicKey.length != PUBLIC_KEY_BYTES) {
			throw new GeneralSecurityException();
		}
		byte[] pkM = new byte[PUBLIC_KEY_BYTES - X25519_KEY_BYTES];
		byte[] pkX = new byte[X25519_KEY_BYTES];
		arraycopy(publicKey, 0, pkM, 0, pkM.length);
		arraycopy(publicKey, pkM.length, pkX, 0, pkX.length);
		SecretWithEncapsulation m;
		try {
			m = new MLKEMGenerator(random).generateEncapsulated(
					new MLKEMPublicKeyParameters(ml_kem_768, pkM));
		} catch (IllegalArgumentException e) {
			throw new GeneralSecurityException(e);
		}
		X25519KeyPairGenerator xGen = new X25519KeyPairGenerator();
		xGen.init(new X25519KeyGenerationParameters(random));
		AsymmetricCipherKeyPair eph = xGen.generateKeyPair();
		byte[] ctX = ((X25519PublicKeyParameters) eph.getPublic())
				.getEncoded();
		byte[] ssX = x25519((X25519PrivateKeyParameters) eph.getPrivate(),
				pkX);
		byte[] ss = combine(m.getSecret(), ssX, ctX, pkX);
		return new Encapsulation(concat(m.getEncapsulation(), ctX), ss);
	}

	public byte[] decapsulate(KeyPair keyPair, byte[] ciphertext)
			throws GeneralSecurityException {
		if (ciphertext.length != CIPHERTEXT_BYTES) {
			throw new GeneralSecurityException();
		}
		byte[] ctM = new byte[CIPHERTEXT_BYTES - X25519_KEY_BYTES];
		byte[] ctX = new byte[X25519_KEY_BYTES];
		arraycopy(ciphertext, 0, ctM, 0, ctM.length);
		arraycopy(ciphertext, ctM.length, ctX, 0, ctX.length);
		// ML-KEM uses implicit rejection: a tampered ciphertext yields an
		// unrelated secret rather than an error
		byte[] ssM = new MLKEMExtractor(keyPair.mlkemPrivate)
				.extractSecret(ctM);
		byte[] ssX = x25519(keyPair.x25519Private, ctX);
		byte[] pkX = keyPair.x25519Private.generatePublicKey().getEncoded();
		return combine(ssM, ssX, ctX, pkX);
	}

	private static byte[] x25519(X25519PrivateKeyParameters priv, byte[] pub)
			throws GeneralSecurityException {
		X25519Agreement agreement = new X25519Agreement();
		agreement.init(priv);
		byte[] out = new byte[agreement.getAgreementSize()];
		try {
			agreement.calculateAgreement(new X25519PublicKeyParameters(pub, 0),
					out, 0);
		} catch (IllegalStateException e) {
			// All-zero output (small-order point)
			throw new GeneralSecurityException(e);
		}
		return out;
	}

	private static byte[] combine(byte[] ssM, byte[] ssX, byte[] ctX,
			byte[] pkX) {
		SHA3Digest sha3 = new SHA3Digest(256);
		sha3.update(ssM, 0, ssM.length);
		sha3.update(ssX, 0, ssX.length);
		sha3.update(ctX, 0, ctX.length);
		sha3.update(pkX, 0, pkX.length);
		sha3.update(LABEL, 0, LABEL.length);
		byte[] out = new byte[SHARED_SECRET_BYTES];
		sha3.doFinal(out, 0);
		return out;
	}

	private static byte[] concat(byte[] a, byte[] b) {
		byte[] out = new byte[a.length + b.length];
		arraycopy(a, 0, out, 0, a.length);
		arraycopy(b, 0, out, a.length, b.length);
		return out;
	}
}
