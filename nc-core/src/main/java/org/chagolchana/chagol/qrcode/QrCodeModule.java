package org.chagolchana.chagol.qrcode;

import org.chagolchana.chagol.api.qrcode.QrCodeClassifier;

import dagger.Module;
import dagger.Provides;

@Module
public class QrCodeModule {

	@Provides
	QrCodeClassifier provideQrCodeClassifier(
			QrCodeClassifierImpl qrCodeClassifier) {
		return qrCodeClassifier;
	}
}
