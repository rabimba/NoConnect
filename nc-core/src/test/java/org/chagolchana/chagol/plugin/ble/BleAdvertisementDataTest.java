package org.chagolchana.chagol.plugin.ble;

import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.TOKEN_BYTES;
import static org.chagolchana.chagol.test.TestUtils.getRandomBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BleAdvertisementDataTest extends ChagolTestCase {

	@Test
	public void testRoundTrip() {
		byte[] token = getRandomBytes(TOKEN_BYTES);
		for (int psm : new int[] {0, 0x80, 0x1001, 0xFFFF}) {
			BleAdvertisementData d = BleAdvertisementData.decode(
					new BleAdvertisementData(token, psm).encode());
			assertNotNull(d);
			assertArrayEquals(token, d.getToken());
			assertEquals(psm, d.getPsm());
		}
	}

	@Test
	public void testMalformedServiceDataIsIgnored() {
		assertNull(BleAdvertisementData.decode(null));
		assertNull(BleAdvertisementData.decode(new byte[0]));
		assertNull(BleAdvertisementData.decode(
				new byte[BleAdvertisementData.LENGTH - 1]));
		assertNull(BleAdvertisementData.decode(
				new byte[BleAdvertisementData.LENGTH + 1]));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongTokenLengthIsRejected() {
		new BleAdvertisementData(new byte[TOKEN_BYTES + 1], 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOutOfRangePsmIsRejected() {
		new BleAdvertisementData(new byte[TOKEN_BYTES], 0x10000);
	}
}
