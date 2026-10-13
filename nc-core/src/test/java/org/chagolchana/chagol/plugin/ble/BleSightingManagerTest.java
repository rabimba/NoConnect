package org.chagolchana.chagol.plugin.ble;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.crypto.CryptoComponentImplFactory;
import org.chagolchana.chagol.plugin.ble.BleSightingManager.Sighting;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.chagolchana.chagol.test.TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.chagolchana.chagol.api.plugin.BleConstants.PROP_SECRET;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.EPOCH_MS;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.epochFor;
import static org.chagolchana.chagol.util.StringUtils.toHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BleSightingManagerTest extends ChagolTestCase {

	private static class MutableClock implements Clock {
		private long now = 0;

		void setCurrentTime(long now) {
			this.now = now;
		}

		@Override
		public long currentTimeMillis() {
			return now;
		}

		@Override
		public void sleep(long milliseconds) {
			now += milliseconds;
		}
	}

	private final MutableClock clock = new MutableClock();
	private final BleRendezvousTokens tokens =
			new BleRendezvousTokens(CryptoComponentImplFactory.create());
	private BleSightingManager<String> sightingManager;

	@Before
	public void setUp() {
		clock.setCurrentTime(1_000_000_000L);
		sightingManager = new BleSightingManager<>(clock, tokens);
	}

	@Test
	public void testAddAndFindNearbyCurrentEpoch() {
		SecretKey secret = TestUtils.getSecretKey();
		long currentEpoch = epochFor(clock.currentTimeMillis());
		byte[] token = tokens.deriveToken(secret, currentEpoch);

		sightingManager.addSighting(token, "device1", 42);

		TransportProperties props = new TransportProperties();
		props.put(PROP_SECRET, toHexString(secret.getBytes()));

		Sighting<String> sighting = sightingManager.findNearby(props);
		assertNotNull(sighting);
		assertEquals("device1", sighting.getDevice());
		assertEquals(42, sighting.getPsm());
		assertEquals(clock.currentTimeMillis(), sighting.getSeen());
	}

	@Test
	public void testFindNearbyAdjacentEpochs() {
		SecretKey secret = TestUtils.getSecretKey();
		long currentEpoch = epochFor(clock.currentTimeMillis());

		// Previous epoch
		byte[] tokenPrev = tokens.deriveToken(secret, currentEpoch - 1);
		sightingManager.addSighting(tokenPrev, "devicePrev", 10);

		TransportProperties props = new TransportProperties();
		props.put(PROP_SECRET, toHexString(secret.getBytes()));

		Sighting<String> sighting = sightingManager.findNearby(props);
		assertNotNull(sighting);
		assertEquals("devicePrev", sighting.getDevice());

		// Next epoch
		sightingManager.clear();
		byte[] tokenNext = tokens.deriveToken(secret, currentEpoch + 1);
		sightingManager.addSighting(tokenNext, "deviceNext", 20);

		sighting = sightingManager.findNearby(props);
		assertNotNull(sighting);
		assertEquals("deviceNext", sighting.getDevice());

		// Two epochs away should not match
		sightingManager.clear();
		byte[] tokenTooOld = tokens.deriveToken(secret, currentEpoch - 2);
		sightingManager.addSighting(tokenTooOld, "deviceOld", 30);
		assertNull(sightingManager.findNearby(props));

		sightingManager.clear();
		byte[] tokenTooFuture = tokens.deriveToken(secret, currentEpoch + 2);
		sightingManager.addSighting(tokenTooFuture, "deviceFuture", 40);
		assertNull(sightingManager.findNearby(props));
	}

	@Test
	public void testFindNearbyWithUnknownOrInvalidSecret() {
		SecretKey secret = TestUtils.getSecretKey();
		long currentEpoch = epochFor(clock.currentTimeMillis());
		byte[] token = tokens.deriveToken(secret, currentEpoch);
		sightingManager.addSighting(token, "device1", 42);

		// Different secret
		SecretKey otherSecret = TestUtils.getSecretKey();
		TransportProperties props = new TransportProperties();
		props.put(PROP_SECRET, toHexString(otherSecret.getBytes()));
		assertNull(sightingManager.findNearby(props));

		// Missing secret
		assertNull(sightingManager.findNearby(new TransportProperties()));

		// Invalid hex
		TransportProperties invalidHexProps = new TransportProperties();
		invalidHexProps.put(PROP_SECRET, "not_a_hex_string");
		assertNull(sightingManager.findNearby(invalidHexProps));

		// Wrong length
		TransportProperties shortProps = new TransportProperties();
		shortProps.put(PROP_SECRET, toHexString(new byte[16]));
		assertNull(sightingManager.findNearby(shortProps));
	}

	@Test
	public void testPruneSightings() {
		SecretKey secret = TestUtils.getSecretKey();
		long currentEpoch = epochFor(clock.currentTimeMillis());
		byte[] token = tokens.deriveToken(secret, currentEpoch);

		sightingManager.addSighting(token, "device1", 42);
		assertEquals(1, sightingManager.size());

		// Advance clock by less than expiry
		clock.setCurrentTime(clock.currentTimeMillis() + EPOCH_MS);
		sightingManager.pruneSightings();
		assertEquals(1, sightingManager.size());

		// Advance past 2 * EPOCH_MS
		clock.setCurrentTime(clock.currentTimeMillis() + 2 * EPOCH_MS);
		sightingManager.pruneSightings();
		assertEquals(0, sightingManager.size());

		TransportProperties props = new TransportProperties();
		props.put(PROP_SECRET, toHexString(secret.getBytes()));
		assertNull(sightingManager.findNearby(props));
	}

	@Test
	public void testClear() {
		sightingManager.addSighting(new byte[8], "device1", 1);
		sightingManager.addSighting(new byte[8], "device2", 2);
		assertEquals(1, sightingManager.size()); // same token overwrites
		sightingManager.clear();
		assertEquals(0, sightingManager.size());
	}
}
