package org.chagolchana.chagol.api.qrcode;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Thrown when a QR code that has been scanned does not have the expected type.
 */
@Immutable
@NotNullByDefault
public class WrongQrCodeTypeException extends FormatException {

	private final QrCodeType qrCodeType;

	public WrongQrCodeTypeException(QrCodeType qrCodeType) {
		this.qrCodeType = qrCodeType;
	}

	public QrCodeType getQrCodeType() {
		return qrCodeType;
	}
}
