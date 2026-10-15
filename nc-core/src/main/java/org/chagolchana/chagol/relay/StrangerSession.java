package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.KeyPair;
import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.relay.NegentropyReconciler.ReconciliationResult;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

/**
 * An isolated session protocol for exchanging sealed envelopes between stranger
 * peers over a fleeting encounter without accessing contact data or personal
 * keys.
 */
@NotNullByDefault
public class StrangerSession implements Runnable {

	private static final Logger LOG =
			getLogger(StrangerSession.class.getName());

	public static final int MAX_TRANSFER_BYTES = 256 * 1024; // 256 KB
	public static final int MAX_ENVELOPES_PER_SESSION = 50;

	private final CryptoComponent crypto;
	private final RelayStore relayStore;
	private final InputStream in;
	private final OutputStream out;
	private final boolean isInitiator;

	private long bytesTransferred = 0;

	public StrangerSession(CryptoComponent crypto, RelayStore relayStore,
			InputStream in, OutputStream out, boolean isInitiator) {
		this.crypto = crypto;
		this.relayStore = relayStore;
		this.in = in;
		this.out = out;
		this.isInitiator = isInitiator;
	}

	@Override
	public void run() {
		try {
			DataInputStream dis = new DataInputStream(in);
			DataOutputStream dos = new DataOutputStream(out);

			// 1. Ephemeral X25519 Link Handshake
			KeyPair ourKp = crypto.generateAgreementKeyPair();
			dos.write(ourKp.getPublic().getEncoded());
			dos.flush();

			byte[] peerPkBytes = new byte[32];
			dis.readFully(peerPkBytes);
			PublicKey peerPk = crypto.getAgreementKeyParser()
					.parsePublicKey(peerPkBytes);
			SecretKey linkKey = crypto.deriveSharedSecret(
					"NoConnect/STRANGER_LINK", peerPk, ourKp);

			// 2. Set Reconciliation & Exchange
			if (isInitiator) {
				runInitiator(dis, dos);
			} else {
				runResponder(dis, dos);
			}

			dos.flush();
		} catch (IOException | GeneralSecurityException e) {
			LOG.log(WARNING, "Stranger session terminated: " + e.getMessage());
		}
	}

	private void runInitiator(DataInputStream dis, DataOutputStream dos)
			throws IOException, FormatException {
		NegentropyReconciler reconciler =
				new NegentropyReconciler(relayStore.getAll());
		byte[] query = reconciler.initiateQuery();
		writeChunk(dos, query);

		byte[] response = readChunk(dis);
		ReconciliationResult result = reconciler.processMessage(response);

		// Alice sends missing envelopes to Bob
		sendEnvelopes(dos, result.getSendIds());

		// Alice receives missing envelopes from Bob
		receiveEnvelopes(dis);
	}

	private void runResponder(DataInputStream dis, DataOutputStream dos)
			throws IOException, FormatException {
		byte[] query = readChunk(dis);
		NegentropyReconciler reconciler =
				new NegentropyReconciler(relayStore.getAll());
		ReconciliationResult result = reconciler.processMessage(query);

		byte[] responseMsg = result.getResponseMessage();
		if (responseMsg == null) {
			responseMsg = new byte[0];
		}
		writeChunk(dos, responseMsg);

		// Bob receives missing envelopes from Alice
		receiveEnvelopes(dis);

		// Bob sends missing envelopes to Alice
		sendEnvelopes(dos, result.getSendIds());
	}

	private void writeChunk(DataOutputStream dos, byte[] chunk)
			throws IOException {
		checkTransferBudget(chunk.length);
		dos.writeInt(chunk.length);
		dos.write(chunk);
		dos.flush();
		bytesTransferred += chunk.length + 4;
	}

	private byte[] readChunk(DataInputStream dis) throws IOException {
		int len = dis.readInt();
		checkTransferBudget(len);
		byte[] b = new byte[len];
		dis.readFully(b);
		bytesTransferred += len + 4;
		return b;
	}

	private void sendEnvelopes(DataOutputStream dos, Set<EnvelopeId> sendIds)
			throws IOException {
		int count = Math.min(sendIds.size(), MAX_ENVELOPES_PER_SESSION);
		dos.writeInt(count);
		dos.flush();

		int sent = 0;
		for (EnvelopeId id : sendIds) {
			if (sent >= count) break;
			SealedEnvelope env = relayStore.get(id);
			if (env == null) continue;

			SealedEnvelope toSend;
			if (env.canSplit()) {
				toSend = env.splitCopies();
			} else {
				toSend = env;
			}
			toSend.decrementHop();

			byte[] wire = toSend.encode();
			writeChunk(dos, wire);
			sent++;
		}
	}

	private void receiveEnvelopes(DataInputStream dis)
			throws IOException, FormatException {
		int count = dis.readInt();
		if (count < 0 || count > MAX_ENVELOPES_PER_SESSION) {
			throw new FormatException();
		}
		for (int i = 0; i < count; i++) {
			byte[] wire = readChunk(dis);
			SealedEnvelope env = SealedEnvelope.decode(wire);
			relayStore.put(env);
		}
	}

	private void checkTransferBudget(int bytes) throws IOException {
		if (bytesTransferred + bytes > MAX_TRANSFER_BYTES) {
			throw new IOException("Exceeded encounter transfer budget");
		}
	}
}
