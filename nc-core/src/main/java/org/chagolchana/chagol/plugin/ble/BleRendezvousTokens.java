package org.chagolchana.chagol.plugin.ble;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.chagolchana.chagol.util.ByteUtils.INT_64_BYTES;
import static org.chagolchana.chagol.util.ByteUtils.writeUint64;

/**
 * Rotating rendezvous tokens for Bluetooth LE discovery. Each pair of
 * contacts shares a secret; in every {@link #EPOCH_MS} period both derive the
 * same short token from it and advertise it. Nearby contacts recognise each
 * other's tokens and connect, while observers see values that change every
 * epoch and can't be linked to each other or to the contact relationship.
 * The token should rotate together with the device's random BLE address,
 * otherwise the address links consecutive tokens.
 * <p>
 * This class only derives and matches tokens. The BLE plugin that advertises
 * and scans them is not implemented yet; see
 * docs/design/ble-discovery.md.
 */
@NotNullByDefault
public class BleRendezvousTokens {

	/**
	 * Token length in bytes. Fits in a legacy (31-byte) advertisement next to
	 * a 128-bit service UUID, leaving room for flags.
	 */
	public static final int TOKEN_BYTES = 8;

	public static final long EPOCH_MS = MINUTES.toMillis(15);

	static final String TOKEN_LABEL =
			"org.chagolchana.chagol.ble/RENDEZVOUS_TOKEN";

	private final CryptoComponent crypto;

	public BleRendezvousTokens(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	public static long epochFor(long timeMs) {
		// Floor division (Math.floorDiv isn't available on all API levels)
		long q = timeMs / EPOCH_MS;
		return timeMs % EPOCH_MS < 0 ? q - 1 : q;
	}

	public byte[] deriveToken(SecretKey sharedKey, long epoch) {
		byte[] epochBytes = new byte[INT_64_BYTES];
		writeUint64(epoch, epochBytes, 0);
		byte[] mac = crypto.mac(TOKEN_LABEL, sharedKey, epochBytes);
		return Arrays.copyOf(mac, TOKEN_BYTES);
	}

	/**
	 * Returns the key of the contact whose token for the current, previous
	 * or next epoch (allowing for clock differences) matches the observed
	 * token, or null if none does.
	 */
	@Nullable
	public <K> K match(byte[] observed, Map<K, SecretKey> contactKeys,
			long nowMs) {
		if (observed.length != TOKEN_BYTES) return null;
		long epoch = epochFor(nowMs);
		for (Map.Entry<K, SecretKey> e : contactKeys.entrySet()) {
			for (long candidate = epoch - 1; candidate <= epoch + 1;
					candidate++) {
				if (Arrays.equals(observed,
						deriveToken(e.getValue(), candidate))) {
					return e.getKey();
				}
			}
		}
		return null;
	}
}
