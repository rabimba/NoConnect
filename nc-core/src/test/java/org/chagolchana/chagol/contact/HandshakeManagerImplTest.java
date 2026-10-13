package org.chagolchana.chagol.contact;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.contact.HandshakeManager.HandshakeResult;
import org.chagolchana.chagol.api.contact.PendingContact;
import org.chagolchana.chagol.api.crypto.KeyPair;
import org.chagolchana.chagol.api.crypto.PrivateKey;
import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.crypto.TransportCrypto;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.record.Record;
import org.chagolchana.chagol.api.record.RecordReader;
import org.chagolchana.chagol.api.record.RecordReader.RecordPredicate;
import org.chagolchana.chagol.api.record.RecordReaderFactory;
import org.chagolchana.chagol.api.record.RecordWriter;
import org.chagolchana.chagol.api.record.RecordWriterFactory;
import org.chagolchana.chagol.api.transport.StreamWriter;
import org.chagolchana.chagol.test.ChagolMockTestCase;
import org.chagolchana.chagol.test.DbExpectations;
import org.chagolchana.chagol.test.PredicateMatcher;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static org.chagolchana.chagol.contact.HandshakeConstants.PROOF_BYTES;
import static org.chagolchana.chagol.contact.HandshakeConstants.PROTOCOL_MAJOR_VERSION;
import static org.chagolchana.chagol.contact.HandshakeConstants.PROTOCOL_MINOR_VERSION;
import static org.chagolchana.chagol.contact.HandshakeRecordTypes.RECORD_TYPE_EPHEMERAL_PUBLIC_KEY;
import static org.chagolchana.chagol.contact.HandshakeRecordTypes.RECORD_TYPE_MINOR_VERSION;
import static org.chagolchana.chagol.contact.HandshakeRecordTypes.RECORD_TYPE_PROOF_OF_OWNERSHIP;
import static org.chagolchana.chagol.test.TestUtils.getAgreementPrivateKey;
import static org.chagolchana.chagol.test.TestUtils.getAgreementPublicKey;
import static org.chagolchana.chagol.test.TestUtils.getPendingContact;
import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.chagolchana.chagol.test.TestUtils.getSecretKey;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HandshakeManagerImplTest extends ChagolMockTestCase {

	private final TransactionManager db =
			context.mock(TransactionManager.class);
	private final IdentityManager identityManager =
			context.mock(IdentityManager.class);
	private final ContactManager contactManager =
			context.mock(ContactManager.class);
	private final TransportCrypto transportCrypto =
			context.mock(TransportCrypto.class);
	private final HandshakeCrypto handshakeCrypto =
			context.mock(HandshakeCrypto.class);
	private final RecordReaderFactory recordReaderFactory =
			context.mock(RecordReaderFactory.class);
	private final RecordWriterFactory recordWriterFactory =
			context.mock(RecordWriterFactory.class);
	private final RecordReader recordReader = context.mock(RecordReader.class);
	private final RecordWriter recordWriter = context.mock(RecordWriter.class);
	private final StreamWriter streamWriter = context.mock(StreamWriter.class);

	private final PendingContact pendingContact = getPendingContact();
	private final PublicKey theirStaticPublicKey =
			pendingContact.getPublicKey();
	private final PublicKey ourStaticPublicKey = getAgreementPublicKey();
	private final PrivateKey ourStaticPrivateKey = getAgreementPrivateKey();
	private final KeyPair ourStaticKeyPair =
			new KeyPair(ourStaticPublicKey, ourStaticPrivateKey);
	private final PublicKey theirEphemeralPublicKey = getAgreementPublicKey();
	private final PublicKey ourEphemeralPublicKey = getAgreementPublicKey();
	private final PrivateKey ourEphemeralPrivateKey = getAgreementPrivateKey();
	private final KeyPair ourEphemeralKeyPair =
			new KeyPair(ourEphemeralPublicKey, ourEphemeralPrivateKey);
	private final SecretKey masterKey = getSecretKey();
	private final byte[] ourProof = getRandomBytes(PROOF_BYTES);
	private final byte[] theirProof = getRandomBytes(PROOF_BYTES);

	private final InputStream in = new ByteArrayInputStream(new byte[0]);
	private final OutputStream out = new ByteArrayOutputStream(0);

	private final HandshakeManagerImpl handshakeManager =
			new HandshakeManagerImpl(db, identityManager, contactManager,
					transportCrypto, handshakeCrypto, recordReaderFactory,
					recordWriterFactory);

	@Test
	public void testHandshakeAsAliceWithPeerVersion_0_1() throws Exception {
		testHandshakeWithPeerVersion_0_1(true);
	}

	@Test
	public void testHandshakeAsBobWithPeerVersion_0_1() throws Exception {
		testHandshakeWithPeerVersion_0_1(false);
	}

	private void testHandshakeWithPeerVersion_0_1(boolean alice)
			throws Exception {
		expectPrepareForHandshake(alice);
		expectSendMinorVersion();
		expectSendKey();
		// Remote peer sends minor version, so use new key derivation
		expectReceiveMinorVersion();
		expectReceiveKey();
		expectDeriveMasterKey_0_1(alice);
		expectDeriveProof(alice);
		expectSendProof();
		expectReceiveProof();
		expectSendEof();
		expectReceiveEof();
		expectVerifyOwnership(alice, true);

		HandshakeResult result = handshakeManager.handshake(
				pendingContact.getId(), in, streamWriter);

		assertArrayEquals(masterKey.getBytes(),
				result.getMasterKey().getBytes());
		assertEquals(alice, result.isAlice());
	}

	@Test
	public void testHandshakeAsAliceWithPeerVersion_0_0() throws Exception {
		testHandshakeWithPeerVersion_0_0(true);
	}

	@Test
	public void testHandshakeAsBobWithPeerVersion_0_0() throws Exception {
		testHandshakeWithPeerVersion_0_0(false);
	}

	private void testHandshakeWithPeerVersion_0_0(boolean alice)
			throws Exception {
		expectPrepareForHandshake(alice);
		expectSendMinorVersion();
		expectSendKey();
		// Remote peer does not send minor version, so use old key derivation
		expectReceiveKey();
		expectDeriveMasterKey_0_0(alice);
		expectDeriveProof(alice);
		expectSendProof();
		expectReceiveProof();
		expectSendEof();
		expectReceiveEof();
		expectVerifyOwnership(alice, true);

		HandshakeResult result = handshakeManager.handshake(
				pendingContact.getId(), in, streamWriter);

		assertArrayEquals(masterKey.getBytes(),
				result.getMasterKey().getBytes());
		assertEquals(alice, result.isAlice());
	}

	@Test(expected = FormatException.class)
	public void testProofOfOwnershipNotVerifiedAsAlice() throws Exception {
		testProofOfOwnershipNotVerified(true);
	}

	@Test(expected = FormatException.class)
	public void testProofOfOwnershipNotVerifiedAsBob() throws Exception {
		testProofOfOwnershipNotVerified(false);
	}

	private void testProofOfOwnershipNotVerified(boolean alice)
			throws Exception {
		expectPrepareForHandshake(alice);
		expectSendMinorVersion();
		expectSendKey();
		expectReceiveMinorVersion();
		expectReceiveKey();
		expectDeriveMasterKey_0_1(alice);
		expectDeriveProof(alice);
		expectSendProof();
		expectReceiveProof();
		expectSendEof();
		expectReceiveEof();
		expectVerifyOwnership(alice, false);

		handshakeManager.handshake(pendingContact.getId(), in, streamWriter);
	}

	private void expectPrepareForHandshake(boolean alice) throws Exception {
		Transaction txn = new Transaction(null, true);

		context.checking(new DbExpectations() {{
			oneOf(db).transactionWithResult(with(true), withDbCallable(txn));
			oneOf(contactManager).getPendingContact(txn,
					pendingContact.getId());
			will(returnValue(pendingContact));
			oneOf(identityManager).getHandshakeKeys(txn);
			will(returnValue(ourStaticKeyPair));
			oneOf(transportCrypto).isAlice(theirStaticPublicKey,
					ourStaticKeyPair);
			will(returnValue(alice));
			oneOf(recordReaderFactory).createRecordReader(in);
			will(returnValue(recordReader));
			oneOf(streamWriter).getOutputStream();
			will(returnValue(out));
			oneOf(recordWriterFactory).createRecordWriter(out);
			will(returnValue(recordWriter));
			oneOf(handshakeCrypto).generateEphemeralKeyPair();
			will(returnValue(ourEphemeralKeyPair));
		}});
	}

	private void expectSendMinorVersion() throws Exception {
		expectWriteRecord(new Record(PROTOCOL_MAJOR_VERSION,
				RECORD_TYPE_MINOR_VERSION,
				new byte[] {PROTOCOL_MINOR_VERSION}));
	}

	private void expectReceiveMinorVersion() throws Exception {
		expectReadRecord(new Record(PROTOCOL_MAJOR_VERSION,
				RECORD_TYPE_MINOR_VERSION,
				new byte[] {PROTOCOL_MINOR_VERSION}));
	}

	private void expectSendKey() throws Exception {
		expectWriteRecord(new Record(PROTOCOL_MAJOR_VERSION,
				RECORD_TYPE_EPHEMERAL_PUBLIC_KEY,
				ourEphemeralPublicKey.getEncoded()));
	}

	private void expectReceiveKey() throws Exception {
		expectReadRecord(new Record(PROTOCOL_MAJOR_VERSION,
				RECORD_TYPE_EPHEMERAL_PUBLIC_KEY,
				theirEphemeralPublicKey.getEncoded()));
	}

	private void expectDeriveMasterKey_0_1(boolean alice) throws Exception {
		context.checking(new Expectations() {{
			oneOf(handshakeCrypto).deriveMasterKey_0_1(theirStaticPublicKey,
					theirEphemeralPublicKey, ourStaticKeyPair,
					ourEphemeralKeyPair, alice);
			will(returnValue(masterKey));
		}});
	}

	private void expectDeriveMasterKey_0_0(boolean alice) throws Exception {
		context.checking(new Expectations() {{
			oneOf(handshakeCrypto).deriveMasterKey_0_0(theirStaticPublicKey,
					theirEphemeralPublicKey, ourStaticKeyPair,
					ourEphemeralKeyPair, alice);
			will(returnValue(masterKey));
		}});
	}

	private void expectDeriveProof(boolean alice) {
		context.checking(new Expectations() {{
			oneOf(handshakeCrypto).proveOwnership(masterKey, alice);
			will(returnValue(ourProof));
		}});
	}

	private void expectSendProof() throws Exception {
		expectWriteRecord(new Record(PROTOCOL_MAJOR_VERSION,
				RECORD_TYPE_PROOF_OF_OWNERSHIP, ourProof));
	}

	private void expectReceiveProof() throws Exception {
		expectReadRecord(new Record(PROTOCOL_MAJOR_VERSION,
				RECORD_TYPE_PROOF_OF_OWNERSHIP, theirProof));
	}

	private void expectSendEof() throws Exception {
		context.checking(new Expectations() {{
			oneOf(streamWriter).sendEndOfStream();
		}});
	}

	private void expectReceiveEof() throws Exception {
		context.checking(new Expectations() {{
			oneOf(recordReader).readRecord(with(any(RecordPredicate.class)),
					with(any(RecordPredicate.class)));
			will(returnValue(null));
		}});
	}

	private void expectVerifyOwnership(boolean alice, boolean verified) {
		context.checking(new Expectations() {{
			oneOf(handshakeCrypto).verifyOwnership(masterKey, !alice,
					theirProof);
			will(returnValue(verified));
		}});
	}

	private void expectWriteRecord(Record record) throws Exception {
		context.checking(new Expectations() {{
			oneOf(recordWriter).writeRecord(with(new PredicateMatcher<>(
					Record.class, r -> recordEquals(record, r))));
			oneOf(recordWriter).flush();
		}});
	}

	private boolean recordEquals(Record expected, Record actual) {
		return expected.getProtocolVersion() == actual.getProtocolVersion() &&
				expected.getRecordType() == actual.getRecordType() &&
				Arrays.equals(expected.getPayload(), actual.getPayload());
	}

	private void expectReadRecord(Record record) throws Exception {
		context.checking(new Expectations() {{
			// Test that the `accept` predicate passed to the reader would
			// accept the expected record
			oneOf(recordReader).readRecord(with(new PredicateMatcher<>(
							RecordPredicate.class, rp -> rp.test(record))),
					with(any(RecordPredicate.class)));
			will(returnValue(record));
		}});
	}
}
