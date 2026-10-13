package org.chagolchana.chagol.keyagreement;

import org.chagolchana.chagol.api.Bytes;
import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.Pair;
import org.chagolchana.chagol.api.UnsupportedVersionException;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.data.BdfReader;
import org.chagolchana.chagol.api.data.BdfReaderFactory;
import org.chagolchana.chagol.api.keyagreement.Payload;
import org.chagolchana.chagol.api.qrcode.QrCodeClassifier;
import org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType;
import org.chagolchana.chagol.api.qrcode.WrongQrCodeTypeException;
import org.chagolchana.chagol.test.ChagolMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.chagolchana.chagol.api.keyagreement.KeyAgreementConstants.COMMIT_LENGTH;
import static org.chagolchana.chagol.api.keyagreement.KeyAgreementConstants.QR_FORMAT_VERSION;
import static org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static org.chagolchana.chagol.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.util.StringUtils.getRandomString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PayloadParserImplTest extends ChagolMockTestCase {

	private final BdfReaderFactory bdfReaderFactory =
			context.mock(BdfReaderFactory.class);
	private final QrCodeClassifier qrCodeClassifier =
			context.mock(QrCodeClassifier.class);
	private final BdfReader bdfReader = context.mock(BdfReader.class);

	private final String payload = getRandomString(123);

	private final PayloadParserImpl payloadParser =
			new PayloadParserImpl(bdfReaderFactory, qrCodeClassifier);

	@Test(expected = WrongQrCodeTypeException.class)
	public void testThrowsExceptionForWrongQrCodeType() throws Exception {
		expectClassifyQrCode(payload, MAILBOX, QR_FORMAT_VERSION);

		payloadParser.parse(payload);
	}

	@Test
	public void testThrowsUnsupportedVersionExceptionForOldVersion()
			throws Exception {
		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION - 1);

		try {
			payloadParser.parse(payload);
			fail();
		} catch (UnsupportedVersionException e) {
			assertTrue(e.isTooOld());
		}
	}

	@Test
	public void testThrowsUnsupportedVersionExceptionForNewVersion()
			throws Exception {
		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION + 1);

		try {
			payloadParser.parse(payload);
			fail();
		} catch (UnsupportedVersionException e) {
			assertFalse(e.isTooOld());
		}
	}

	@Test(expected = FormatException.class)
	public void testThrowsFormatExceptionForEmptyList() throws Exception {
		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION);

		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(
					with(any(ByteArrayInputStream.class)));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(new BdfList()));
		}});

		payloadParser.parse(payload);
	}

	@Test(expected = FormatException.class)
	public void testThrowsFormatExceptionForDataAfterList()
			throws Exception {
		byte[] commitment = getRandomBytes(COMMIT_LENGTH);

		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION);

		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(
					with(any(ByteArrayInputStream.class)));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(BdfList.of(new Bytes(commitment))));
			oneOf(bdfReader).eof();
			will(returnValue(false));
		}});

		payloadParser.parse(payload);
	}

	@Test(expected = FormatException.class)
	public void testThrowsFormatExceptionForShortCommitment()
			throws Exception {
		byte[] commitment = getRandomBytes(COMMIT_LENGTH - 1);

		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION);
		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(
					with(any(ByteArrayInputStream.class)));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(BdfList.of(new Bytes(commitment))));
			oneOf(bdfReader).eof();
			will(returnValue(true));
		}});

		payloadParser.parse(payload);
	}

	@Test(expected = FormatException.class)
	public void testThrowsFormatExceptionForLongCommitment()
			throws Exception {
		byte[] commitment = getRandomBytes(COMMIT_LENGTH + 1);

		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION);
		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(
					with(any(ByteArrayInputStream.class)));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(BdfList.of(new Bytes(commitment))));
			oneOf(bdfReader).eof();
			will(returnValue(true));
		}});

		payloadParser.parse(payload);
	}

	@Test
	public void testAcceptsPayloadWithNoDescriptors() throws Exception {
		byte[] commitment = getRandomBytes(COMMIT_LENGTH);

		expectClassifyQrCode(payload, BQP, QR_FORMAT_VERSION);
		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(
					with(any(ByteArrayInputStream.class)));
			will(returnValue(bdfReader));
			oneOf(bdfReader).readList();
			will(returnValue(BdfList.of(new Bytes(commitment))));
			oneOf(bdfReader).eof();
			will(returnValue(true));
		}});

		Payload p = payloadParser.parse(payload);
		assertArrayEquals(commitment, p.getCommitment());
		assertTrue(p.getTransportDescriptors().isEmpty());
	}

	private void expectClassifyQrCode(String payload, QrCodeType qrCodeType,
			int formatVersion) {
		context.checking(new Expectations() {{
			oneOf(qrCodeClassifier).classifyQrCode(payload);
			will(returnValue(new Pair<>(qrCodeType, formatVersion)));
		}});
	}
}
