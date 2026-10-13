package org.chagolchana.chagol.contact;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.UnsupportedVersionException;
import org.chagolchana.chagol.api.contact.PendingContact;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.KeyParser;
import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.test.ChagolMockTestCase;
import org.chagolchana.chagol.util.Base32;
import org.jmock.Expectations;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.util.Locale;

import static java.lang.System.arraycopy;
import static org.chagolchana.chagol.api.contact.HandshakeLinkConstants.BASE32_LINK_BYTES;
import static org.chagolchana.chagol.api.contact.HandshakeLinkConstants.FORMAT_VERSION;
import static org.chagolchana.chagol.api.contact.HandshakeLinkConstants.ID_LABEL;
import static org.chagolchana.chagol.api.contact.HandshakeLinkConstants.RAW_LINK_BYTES;
import static org.chagolchana.chagol.api.crypto.CryptoConstants.KEY_TYPE_AGREEMENT;
import static org.chagolchana.chagol.api.crypto.CryptoConstants.KEY_TYPE_SIGNATURE;
import static org.chagolchana.chagol.api.crypto.CryptoConstants.MAX_AGREEMENT_PUBLIC_KEY_BYTES;
import static org.chagolchana.chagol.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static org.chagolchana.chagol.test.TestUtils.getAgreementPublicKey;
import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.test.TestUtils.getRandomId;
import static org.chagolchana.chagol.util.StringUtils.getRandomString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class PendingContactFactoryImplTest extends ChagolMockTestCase {

	private final CryptoComponent crypto = context.mock(CryptoComponent.class);
	private final Clock clock = context.mock(Clock.class);
	private final KeyParser keyParser = context.mock(KeyParser.class);

	private final PendingContactFactory pendingContactFactory =
			new PendingContactFactoryImpl(crypto, clock);
	private final String alias = getRandomString(MAX_AUTHOR_NAME_LENGTH);
	private final PublicKey publicKey = getAgreementPublicKey();
	private final byte[] idBytes = getRandomId();
	private final long timestamp = System.currentTimeMillis();

	@Test(expected = FormatException.class)
	public void testRejectsSyntacticallyInvalidLink() throws Exception {
		pendingContactFactory.createPendingContact("noconnect://potato", alias);
	}

	@Test
	public void testRejectsLinkWithUnknownFormatVersion() throws Exception {
		String link = encodeLink(FORMAT_VERSION + 1);
		try {
			pendingContactFactory.createPendingContact(link, alias);
			fail();
		} catch (UnsupportedVersionException e) {
			assertFalse(e.isTooOld());
		}
	}

	@Test(expected = FormatException.class)
	public void testRejectsLinkWithInvalidPublicKey() throws Exception {
		context.checking(new Expectations() {{
			oneOf(crypto).getAgreementKeyParser();
			will(returnValue(keyParser));
			oneOf(keyParser).parsePublicKey(publicKey.getEncoded());
			will(throwException(new GeneralSecurityException()));
		}});

		pendingContactFactory.createPendingContact(encodeLink(), alias);
	}

	@Test
	public void testAcceptsValidLinkWithoutPrefix() throws Exception {
		testAcceptsValidLink(encodeLink());
	}

	@Test
	public void testAcceptsValidLinkWithPrefix() throws Exception {
		testAcceptsValidLink("noconnect://" + encodeLink());
	}

	@Test
	public void testAcceptsValidLinkWithRubbish() throws Exception {
		testAcceptsValidLink("before " + encodeLink() + " after");
	}

	@Test
	public void testAcceptsValidLinkWithPrefixAndRubbish() throws Exception {
		testAcceptsValidLink("before noconnect://" + encodeLink() + " after");
	}

	private void testAcceptsValidLink(String link) throws Exception {
		context.checking(new Expectations() {{
			oneOf(crypto).getAgreementKeyParser();
			will(returnValue(keyParser));
			oneOf(keyParser).parsePublicKey(publicKey.getEncoded());
			will(returnValue(publicKey));
			oneOf(crypto).hash(ID_LABEL, publicKey.getEncoded());
			will(returnValue(idBytes));
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp));
		}});

		PendingContact p =
				pendingContactFactory.createPendingContact(link, alias);
		assertArrayEquals(idBytes, p.getId().getBytes());
		assertArrayEquals(publicKey.getEncoded(),
				p.getPublicKey().getEncoded());
		assertEquals(alias, p.getAlias());
		assertEquals(timestamp, p.getTimestamp());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateHandshakeLinkRejectsInvalidKeyType() {
		PublicKey invalidPublicKey = context.mock(PublicKey.class);

		context.checking(new Expectations() {{
			oneOf(invalidPublicKey).getKeyType();
			will(returnValue(KEY_TYPE_SIGNATURE));
		}});

		pendingContactFactory.createHandshakeLink(invalidPublicKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateHandshakeLinkRejectsInvalidKeyLength() {
		PublicKey invalidPublicKey = context.mock(PublicKey.class);
		byte[] invalidPublicKeyBytes =
				getRandomBytes(MAX_AGREEMENT_PUBLIC_KEY_BYTES + 1);

		context.checking(new Expectations() {{
			oneOf(invalidPublicKey).getKeyType();
			will(returnValue(KEY_TYPE_AGREEMENT));
			oneOf(invalidPublicKey).getEncoded();
			will(returnValue(invalidPublicKeyBytes));
		}});

		pendingContactFactory.createHandshakeLink(invalidPublicKey);
	}

	@Test
	public void testCreateAndParseLink() throws Exception {
		context.checking(new Expectations() {{
			oneOf(crypto).getAgreementKeyParser();
			will(returnValue(keyParser));
			oneOf(keyParser).parsePublicKey(publicKey.getEncoded());
			will(returnValue(publicKey));
			oneOf(crypto).hash(ID_LABEL, publicKey.getEncoded());
			will(returnValue(idBytes));
			oneOf(clock).currentTimeMillis();
			will(returnValue(timestamp));
		}});

		String link = pendingContactFactory.createHandshakeLink(publicKey);
		PendingContact p =
				pendingContactFactory.createPendingContact(link, alias);
		assertArrayEquals(idBytes, p.getId().getBytes());
		assertArrayEquals(publicKey.getEncoded(),
				p.getPublicKey().getEncoded());
		assertEquals(alias, p.getAlias());
		assertEquals(timestamp, p.getTimestamp());
	}

	private String encodeLink() {
		return encodeLink(FORMAT_VERSION);
	}

	private String encodeLink(int formatVersion) {
		byte[] rawLink = new byte[RAW_LINK_BYTES];
		rawLink[0] = (byte) formatVersion;
		byte[] publicKeyBytes = publicKey.getEncoded();
		arraycopy(publicKeyBytes, 0, rawLink, 1, publicKeyBytes.length);
		String base32 = Base32.encode(rawLink).toLowerCase(Locale.US);
		assertEquals(BASE32_LINK_BYTES, base32.length());
		return base32;
	}
}
