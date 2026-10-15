package org.chagolchana.chagol.relay;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import static org.chagolchana.chagol.util.ByteUtils.writeUint32;

/**
 * Range-based set reconciliation engine (Negentropy style).
 * Allows two passing off-grid devices to discover missing envelope sets in 1-2
 * round trips.
 */
@NotNullByDefault
public class NegentropyReconciler {

	private static final byte MODE_EQUAL = 0x00;
	private static final byte MODE_FINGERPRINT = 0x01;
	private static final byte MODE_IDS = 0x02;

	public static class ReconciliationResult {
		private final Set<EnvelopeId> needIds;
		private final Set<EnvelopeId> sendIds;
		@Nullable
		private final byte[] responseMessage;

		public ReconciliationResult(Set<EnvelopeId> needIds,
				Set<EnvelopeId> sendIds, @Nullable byte[] responseMessage) {
			this.needIds = needIds;
			this.sendIds = sendIds;
			this.responseMessage = responseMessage;
		}

		public Set<EnvelopeId> getNeedIds() {
			return needIds;
		}

		public Set<EnvelopeId> getSendIds() {
			return sendIds;
		}

		public boolean hasResponseMessage() {
			return responseMessage != null;
		}

		@Nullable
		public byte[] getResponseMessage() {
			return responseMessage;
		}
	}

	private final List<SealedEnvelope> items;

	public NegentropyReconciler(Collection<SealedEnvelope> items) {
		this.items = new ArrayList<>(items);
		Collections.sort(this.items, new Comparator<SealedEnvelope>() {
			@Override
			public int compare(SealedEnvelope a, SealedEnvelope b) {
				if (a.getExpiry() < b.getExpiry()) return -1;
				if (a.getExpiry() > b.getExpiry()) return 1;
				return a.getId().compareTo(b.getId());
			}
		});
	}

	private byte[] computeFingerprint(List<SealedEnvelope> list) {
		byte[] fp = new byte[16];
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] expBytes = new byte[4];
			for (SealedEnvelope env : list) {
				md.reset();
				md.update(env.getId().getBytes());
				writeUint32(env.getExpiry(), expBytes, 0);
				md.update(expBytes);
				byte[] h = md.digest();
				for (int i = 0; i < 16; i++) {
					fp[i] ^= h[i];
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
		return fp;
	}

	public byte[] initiateQuery() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		try {
			if (items.size() <= 16) {
				out.writeByte(MODE_IDS);
				out.writeInt(items.size());
				for (SealedEnvelope env : items) {
					out.write(env.getId().getBytes());
				}
			} else {
				out.writeByte(MODE_FINGERPRINT);
				out.writeInt(items.size());
				out.write(computeFingerprint(items));
			}
			out.flush();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		return baos.toByteArray();
	}

	public ReconciliationResult processMessage(byte[] message) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
		Set<EnvelopeId> need = new HashSet<>();
		Set<EnvelopeId> send = new HashSet<>();
		byte[] response = null;

		try {
			byte mode = in.readByte();
			if (mode == MODE_EQUAL) {
				return new ReconciliationResult(need, send, null);
			} else if (mode == MODE_FINGERPRINT) {
				int count = in.readInt();
				byte[] peerFp = new byte[16];
				in.readFully(peerFp);

				byte[] localFp = computeFingerprint(items);
				if (count == items.size() && Arrays.equals(peerFp, localFp)) {
					// Perfectly equal!
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					baos.write(MODE_EQUAL);
					response = baos.toByteArray();
				} else {
					// Differ: respond with full list of local IDs so peer can compute difference
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(baos);
					out.writeByte(MODE_IDS);
					out.writeInt(items.size());
					for (SealedEnvelope env : items) {
						out.write(env.getId().getBytes());
					}
					out.flush();
					response = baos.toByteArray();
				}
			} else if (mode == MODE_IDS) {
				int count = in.readInt();
				Set<EnvelopeId> peerIds = new HashSet<>(count);
				byte[] idBuf = new byte[EnvelopeId.LENGTH];
				for (int i = 0; i < count; i++) {
					in.readFully(idBuf);
					peerIds.add(new EnvelopeId(idBuf.clone()));
				}

				Set<EnvelopeId> localIds = new HashSet<>(items.size());
				for (SealedEnvelope env : items) {
					localIds.add(env.getId());
				}

				// What peer has that we don't
				for (EnvelopeId pid : peerIds) {
					if (!localIds.contains(pid)) {
						need.add(pid);
					}
				}

				// What we have that peer doesn't
				for (EnvelopeId lid : localIds) {
					if (!peerIds.contains(lid)) {
						send.add(lid);
					}
				}

				// Send back our local IDs if this was a query
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(baos);
				out.writeByte(MODE_IDS);
				out.writeInt(items.size());
				for (SealedEnvelope env : items) {
					out.write(env.getId().getBytes());
				}
				out.flush();
				response = baos.toByteArray();
			}
		} catch (IOException e) {
			throw new AssertionError(e);
		}

		return new ReconciliationResult(need, send, response);
	}
}
