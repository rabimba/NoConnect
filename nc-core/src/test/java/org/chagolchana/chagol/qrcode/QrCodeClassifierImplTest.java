package org.chagolchana.chagol.qrcode;

import org.chagolchana.chagol.api.Pair;
import org.chagolchana.chagol.api.keyagreement.KeyAgreementConstants;
import org.chagolchana.chagol.api.mailbox.MailboxConstants;
import org.chagolchana.chagol.api.qrcode.QrCodeClassifier;
import org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import static org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType.UNKNOWN;
import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.util.StringUtils.ISO_8859_1;
import static org.junit.Assert.assertEquals;

public class QrCodeClassifierImplTest extends ChagolTestCase {

	private final QrCodeClassifier classifier = new QrCodeClassifierImpl();

	@Test
	public void testClassifiesEmptyStringAsUnknown() {
		Pair<QrCodeType, Integer> result = classifier.classifyQrCode("");
		assertEquals(UNKNOWN, result.getFirst());
		assertEquals(0, result.getSecond().intValue());
	}

	@Test
	public void testClassifiesKeyAgreement() {
		byte[] payloadBytes = getRandomBytes(123);
		for (int version = 0; version < 32; version++) {
			int typeAndVersion =
					(KeyAgreementConstants.QR_FORMAT_ID << 5) | version;
			payloadBytes[0] = (byte) typeAndVersion;
			String payload = new String(payloadBytes, ISO_8859_1);
			Pair<QrCodeType, Integer> result =
					classifier.classifyQrCode(payload);
			assertEquals(BQP, result.getFirst());
			assertEquals(version, result.getSecond().intValue());
		}
	}

	@Test
	public void testClassifiesMailbox() {
		byte[] payloadBytes = getRandomBytes(123);
		for (int version = 0; version < 32; version++) {
			int typeAndVersion =
					(MailboxConstants.QR_FORMAT_ID << 5) | version;
			payloadBytes[0] = (byte) typeAndVersion;
			String payload = new String(payloadBytes, ISO_8859_1);
			Pair<QrCodeType, Integer> result =
					classifier.classifyQrCode(payload);
			assertEquals(MAILBOX, result.getFirst());
			assertEquals(version, result.getSecond().intValue());
		}
	}

	@Test
	public void testClassifiesUnknownFormatIdAsUnknown() {
		byte[] payloadBytes = getRandomBytes(123);
		int unknownFormatId = MailboxConstants.QR_FORMAT_ID + 1;
		int typeAndVersion = unknownFormatId << 5;
		payloadBytes[0] = (byte) typeAndVersion;
		String payload = new String(payloadBytes, ISO_8859_1);
		Pair<QrCodeType, Integer> result = classifier.classifyQrCode(payload);
		assertEquals(UNKNOWN, result.getFirst());
		assertEquals(0, result.getSecond().intValue());
	}
}
