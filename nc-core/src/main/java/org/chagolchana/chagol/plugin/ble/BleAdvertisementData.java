package org.chagolchana.chagol.plugin.ble;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Arrays;

import javax.annotation.Nullable;

import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.TOKEN_BYTES;
import static org.chagolchana.chagol.util.ByteUtils.readUint16;
import static org.chagolchana.chagol.util.ByteUtils.writeUint16;

/**
 * The service data in a NoConnect BLE advertisement: an 8-byte rendezvous
 * token followed by the 16-bit L2CAP channel number (PSM) to connect to.
 * With the 128-bit service UUID and the flags field this exactly fills a
 * 31-byte legacy advertisement.
 */
@NotNullByDefault
public class BleAdvertisementData {

	public static final int LENGTH = TOKEN_BYTES + 2;

	private final byte[] token;
	private final int psm;

	public BleAdvertisementData(byte[] token, int psm) {
		if (token.length != TOKEN_BYTES) throw new IllegalArgumentException();
		if (psm < 0 || psm > 0xFFFF) throw new IllegalArgumentException();
		this.token = token.clone();
		this.psm = psm;
	}

	public byte[] getToken() {
		return token.clone();
	}

	public int getPsm() {
		return psm;
	}

	public byte[] encode() {
		byte[] out = new byte[LENGTH];
		System.arraycopy(token, 0, out, 0, TOKEN_BYTES);
		writeUint16(psm, out, TOKEN_BYTES);
		return out;
	}

	/**
	 * Returns the decoded data, or null if the service data is malformed.
	 * The input comes from any nearby device, so nothing is assumed about it.
	 */
	@Nullable
	public static BleAdvertisementData decode(@Nullable byte[] serviceData) {
		if (serviceData == null || serviceData.length != LENGTH) return null;
		return new BleAdvertisementData(
				Arrays.copyOf(serviceData, TOKEN_BYTES),
				readUint16(serviceData, TOKEN_BYTES));
	}
}
