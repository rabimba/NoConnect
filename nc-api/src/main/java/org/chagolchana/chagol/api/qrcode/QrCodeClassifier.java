package org.chagolchana.chagol.api.qrcode;

import org.chagolchana.chagol.api.Pair;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface QrCodeClassifier {

	enum QrCodeType {
		BQP,
		MAILBOX,
		UNKNOWN
	}

	Pair<QrCodeType, Integer> classifyQrCode(String payload);
}
