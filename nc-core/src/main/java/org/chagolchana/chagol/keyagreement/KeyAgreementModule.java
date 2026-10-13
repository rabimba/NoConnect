package org.chagolchana.chagol.keyagreement;

import org.chagolchana.chagol.api.keyagreement.KeyAgreementTask;
import org.chagolchana.chagol.api.keyagreement.PayloadEncoder;
import org.chagolchana.chagol.api.keyagreement.PayloadParser;

import dagger.Module;
import dagger.Provides;

@Module
public class KeyAgreementModule {

	@Provides
	KeyAgreementTask provideKeyAgreementTask(
			KeyAgreementTaskImpl keyAgreementTask) {
		return keyAgreementTask;
	}

	@Provides
	PayloadEncoder providePayloadEncoder(PayloadEncoderImpl payloadEncoder) {
		return payloadEncoder;
	}

	@Provides
	PayloadParser providePayloadParser(PayloadParserImpl payloadParser) {
		return payloadParser;
	}

	@Provides
	ConnectionChooser provideConnectionChooser(
			ConnectionChooserImpl connectionChooser) {
		return connectionChooser;
	}
}
