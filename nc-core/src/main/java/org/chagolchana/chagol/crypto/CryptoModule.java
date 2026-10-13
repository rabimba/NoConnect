package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.KeyAgreementCrypto;
import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator;
import org.chagolchana.chagol.api.crypto.StreamDecrypterFactory;
import org.chagolchana.chagol.api.crypto.StreamEncrypterFactory;
import org.chagolchana.chagol.api.crypto.TransportCrypto;
import org.chagolchana.chagol.api.system.SecureRandomProvider;

import java.security.SecureRandom;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CryptoModule {

	@Provides
	AuthenticatedCipher provideAuthenticatedCipher() {
		return new XSalsa20Poly1305AuthenticatedCipher();
	}

	@Provides
	@Singleton
	CryptoComponent provideCryptoComponent(
			SecureRandomProvider secureRandomProvider,
			ScryptKdf passwordBasedKdf) {
		return new CryptoComponentImpl(secureRandomProvider, passwordBasedKdf);
	}

	@Provides
	PasswordStrengthEstimator providePasswordStrengthEstimator() {
		return new PasswordStrengthEstimatorImpl();
	}

	@Provides
	TransportCrypto provideTransportCrypto(
			TransportCryptoImpl transportCrypto) {
		return transportCrypto;
	}

	@Provides
	StreamDecrypterFactory provideStreamDecrypterFactory(
			Provider<AuthenticatedCipher> cipherProvider) {
		return new StreamDecrypterFactoryImpl(cipherProvider);
	}

	@Provides
	StreamEncrypterFactory provideStreamEncrypterFactory(
			CryptoComponent crypto, TransportCrypto transportCrypto,
			Provider<AuthenticatedCipher> cipherProvider) {
		return new StreamEncrypterFactoryImpl(crypto, transportCrypto,
				cipherProvider);
	}

	@Provides
	KeyAgreementCrypto provideKeyAgreementCrypto(
			KeyAgreementCryptoImpl keyAgreementCrypto) {
		return keyAgreementCrypto;
	}

	@Provides
	SecureRandom getSecureRandom(CryptoComponent crypto) {
		return crypto.getSecureRandom();
	}

}
