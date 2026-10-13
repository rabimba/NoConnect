package org.chagolchana.chagol.plugin.ble;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import static org.chagolchana.chagol.api.plugin.BleConstants.PROP_SECRET;
import static org.chagolchana.chagol.api.plugin.BleConstants.SECRET_BYTES;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.EPOCH_MS;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.epochFor;
import static org.chagolchana.chagol.util.StringUtils.fromHexString;
import static org.chagolchana.chagol.util.StringUtils.toHexString;

/**
 * Manages received BLE discovery sightings and matches them against contacts'
 * rotating rendezvous tokens across epoch boundaries.
 */
@ThreadSafe
@NotNullByDefault
public class BleSightingManager<T> {

	public static final long DEFAULT_EXPIRY_MS = 2 * EPOCH_MS;

	public static class Sighting<T> {

		private final T device;
		private final int psm;
		private final long seen;

		public Sighting(T device, int psm, long seen) {
			this.device = device;
			this.psm = psm;
			this.seen = seen;
		}

		public T getDevice() {
			return device;
		}

		public int getPsm() {
			return psm;
		}

		public long getSeen() {
			return seen;
		}
	}

	private final Clock clock;
	private final BleRendezvousTokens tokens;
	private final long expiryMs;
	private final Map<String, Sighting<T>> sightings =
			new ConcurrentHashMap<>();

	public BleSightingManager(Clock clock, BleRendezvousTokens tokens) {
		this(clock, tokens, DEFAULT_EXPIRY_MS);
	}

	public BleSightingManager(Clock clock, BleRendezvousTokens tokens,
			long expiryMs) {
		this.clock = clock;
		this.tokens = tokens;
		this.expiryMs = expiryMs;
	}

	public void addSighting(byte[] token, T device, int psm) {
		addSighting(token, device, psm, clock.currentTimeMillis());
	}

	public void addSighting(byte[] token, T device, int psm, long timestamp) {
		sightings.put(toHexString(token),
				new Sighting<>(device, psm, timestamp));
	}

	/**
	 * Returns the sighting for a contact with the given transport properties if
	 * seen in the current, previous, or next epoch, or null if not seen.
	 */
	@Nullable
	public Sighting<T> findNearby(TransportProperties p) {
		String secretHex = p.get(PROP_SECRET);
		if (secretHex == null) return null;
		SecretKey secret;
		try {
			byte[] b = fromHexString(secretHex);
			if (b.length != SECRET_BYTES) return null;
			secret = new SecretKey(b);
		} catch (FormatException e) {
			return null;
		}
		long epoch = epochFor(clock.currentTimeMillis());
		for (long e = epoch - 1; e <= epoch + 1; e++) {
			Sighting<T> s = sightings.get(toHexString(tokens.deriveToken(secret,
					e)));
			if (s != null) return s;
		}
		return null;
	}

	public void pruneSightings() {
		long now = clock.currentTimeMillis();
		Iterator<Sighting<T>> it = sightings.values().iterator();
		while (it.hasNext()) {
			if (now - it.next().getSeen() > expiryMs) it.remove();
		}
	}

	public void clear() {
		sightings.clear();
	}

	public int size() {
		return sightings.size();
	}
}
