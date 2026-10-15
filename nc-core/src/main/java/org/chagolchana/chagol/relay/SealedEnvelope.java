package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.annotation.concurrent.NotThreadSafe;

import static org.chagolchana.chagol.util.ByteUtils.readUint16;
import static org.chagolchana.chagol.util.ByteUtils.readUint32;
import static org.chagolchana.chagol.util.ByteUtils.writeUint16;
import static org.chagolchana.chagol.util.ByteUtils.writeUint32;

/**
 * A sealed delay-tolerant envelope for multi-hop store-and-forward relay.
 * Intermediate stranger nodes carry the padded ciphertext without access to
 * plaintext, sender identity, or recipient identity.
 */
@NotThreadSafe
@NotNullByDefault
public class SealedEnvelope {

	public static final byte VERSION = 0x01;
	public static final int RECIPIENT_TAG_BYTES = 16;
	public static final int HEADER_BYTES = 25;

	public static final int TIER_SMALL = 1024;
	public static final int TIER_MEDIUM = 4096;
	public static final int TIER_LARGE = 16384;

	private final EnvelopeId id;
	private final byte[] recipientTag;
	private final long expiry; // unix timestamp in seconds (uint32)
	private int hopLimit;
	private int copies;
	private final int payloadLength;
	private final byte[] paddedCiphertext;

	public SealedEnvelope(EnvelopeId id, byte[] recipientTag, long expiry,
			int hopLimit, int copies, int payloadLength,
			byte[] paddedCiphertext) {
		this.id = id;
		this.recipientTag = recipientTag;
		this.expiry = expiry;
		this.hopLimit = hopLimit;
		this.copies = copies;
		this.payloadLength = payloadLength;
		this.paddedCiphertext = paddedCiphertext;
	}

	public static SealedEnvelope create(byte[] recipientTag, long expiry,
			int hopLimit, int copies, byte[] payload) {
		if (recipientTag.length != RECIPIENT_TAG_BYTES) {
			throw new IllegalArgumentException("Invalid recipient tag length");
		}
		if (payload.length > TIER_LARGE) {
			throw new IllegalArgumentException("Payload exceeds maximum tier");
		}
		int tierSize;
		if (payload.length <= TIER_SMALL) {
			tierSize = TIER_SMALL;
		} else if (payload.length <= TIER_MEDIUM) {
			tierSize = TIER_MEDIUM;
		} else {
			tierSize = TIER_LARGE;
		}

		byte[] padded = new byte[tierSize];
		System.arraycopy(payload, 0, padded, 0, payload.length);
		EnvelopeId id = calculateId(recipientTag, expiry, padded);

		return new SealedEnvelope(id, recipientTag, expiry, hopLimit, copies,
				payload.length, padded);
	}

	public static EnvelopeId calculateId(byte[] recipientTag, long expiry,
			byte[] paddedCiphertext) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(recipientTag);
			byte[] expBytes = new byte[4];
			writeUint32(expiry, expBytes, 0);
			digest.update(expBytes);
			digest.update(paddedCiphertext);
			return new EnvelopeId(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

	public byte[] encode() {
		ByteBuffer buf = ByteBuffer.allocate(HEADER_BYTES + paddedCiphertext.length);
		buf.put(VERSION);
		buf.put(recipientTag);
		byte[] expBytes = new byte[4];
		writeUint32(expiry, expBytes, 0);
		buf.put(expBytes);
		buf.put((byte) (hopLimit & 0xFF));
		buf.put((byte) (copies & 0xFF));
		byte[] lenBytes = new byte[2];
		writeUint16(payloadLength, lenBytes, 0);
		buf.put(lenBytes);
		buf.put(paddedCiphertext);
		return buf.array();
	}

	public static SealedEnvelope decode(byte[] wire) throws FormatException {
		if (wire.length < HEADER_BYTES + TIER_SMALL) {
			throw new FormatException();
		}
		ByteBuffer buf = ByteBuffer.wrap(wire);
		byte version = buf.get();
		if (version != VERSION) {
			throw new FormatException();
		}
		byte[] tag = new byte[RECIPIENT_TAG_BYTES];
		buf.get(tag);
		byte[] expBytes = new byte[4];
		buf.get(expBytes);
		long expiry = readUint32(expBytes, 0);
		int hop = buf.get() & 0xFF;
		int copies = buf.get() & 0xFF;
		byte[] lenBytes = new byte[2];
		buf.get(lenBytes);
		int payloadLen = readUint16(lenBytes, 0);

		int ciphertextLen = wire.length - HEADER_BYTES;
		if (ciphertextLen != TIER_SMALL && ciphertextLen != TIER_MEDIUM
				&& ciphertextLen != TIER_LARGE) {
			throw new FormatException();
		}
		if (payloadLen > ciphertextLen) {
			throw new FormatException();
		}

		byte[] padded = new byte[ciphertextLen];
		buf.get(padded);
		EnvelopeId id = calculateId(tag, expiry, padded);

		return new SealedEnvelope(id, tag, expiry, hop, copies, payloadLen,
				padded);
	}

	public boolean canSplit() {
		return copies > 1;
	}

	/**
	 * Binary Spray-and-Wait copy split:
	 * Allocates half of the remaining copies to a new envelope instance,
	 * keeping the remainder in this instance.
	 */
	public SealedEnvelope splitCopies() {
		if (!canSplit()) {
			throw new IllegalStateException("Cannot split envelope with 1 copy");
		}
		int half = copies / 2;
		int otherHalf = copies - half;
		this.copies = half;
		return new SealedEnvelope(id, recipientTag, expiry, hopLimit, otherHalf,
				payloadLength, paddedCiphertext);
	}

	public void decrementHop() {
		if (hopLimit > 0) {
			hopLimit--;
		}
	}

	public EnvelopeId getId() {
		return id;
	}

	public byte[] getRecipientTag() {
		return recipientTag;
	}

	public long getExpiry() {
		return expiry;
	}

	public int getHopLimit() {
		return hopLimit;
	}

	public int getCopies() {
		return copies;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public byte[] getPaddedCiphertext() {
		return paddedCiphertext;
	}

	public byte[] getPayload() {
		return Arrays.copyOf(paddedCiphertext, payloadLength);
	}
}
