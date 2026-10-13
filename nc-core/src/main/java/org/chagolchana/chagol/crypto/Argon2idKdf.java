package org.chagolchana.chagol.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.util.StringUtils;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.logging.Logger;

import javax.inject.Inject;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.util.LogUtils.logDuration;
import static org.chagolchana.chagol.util.LogUtils.now;

/**
 * Argon2id (RFC 9106) with t = 3 passes and p = 1 lane. The cost parameter is
 * the memory size in KiB, calibrated so that deriving a key takes about
 * {@link #TARGET_MS} on this device, within [{@link #MIN_COST},
 * {@link #MAX_COST}] and never more than a quarter of the max heap.
 */
@NotNullByDefault
class Argon2idKdf implements PasswordBasedKdf {

	private static final Logger LOG = getLogger(Argon2idKdf.class.getName());

	static final int MIN_COST = 64 * 1024; // 64 MiB, RFC 9106 second choice
	static final int MAX_COST = 1024 * 1024; // 1 GiB
	/**
	 * Smallest memory size ever used or accepted (8 MiB). Below
	 * {@link #MIN_COST} so devices with a small heap still get a usable KDF.
	 */
	static final int MIN_DECRYPT_COST = 8 * 1024;
	static final int ITERATIONS = 3;
	static final int PARALLELISM = 1;
	private static final int TARGET_MS = 1000;

	private final Clock clock;

	@Inject
	Argon2idKdf(Clock clock) {
		this.clock = clock;
	}

	@Override
	public int chooseCostParameter() {
		int maxCost = maxCostForHeap();
		int cost = max(MIN_DECRYPT_COST, min(MIN_COST, maxCost));
		while (cost * 2L <= maxCost && measureDuration(cost) * 2 <= TARGET_MS) {
			cost *= 2;
		}
		if (LOG.isLoggable(INFO)) LOG.info("Argon2id memory " + cost + " KiB");
		return cost;
	}

	/**
	 * Returns true if the given cost parameter is one this KDF will use to
	 * derive a key. Rejecting out-of-range values stops a tampered key file
	 * from exhausting memory.
	 */
	static boolean isValidCost(long cost) {
		return cost >= MIN_DECRYPT_COST && cost <= MAX_COST;
	}

	private static int maxCostForHeap() {
		long quarterHeapKiB = Runtime.getRuntime().maxMemory() / 4 / 1024;
		int max = (int) min(MAX_COST, quarterHeapKiB);
		if (LOG.isLoggable(INFO) && max < MAX_COST) {
			LOG.info("Argon2id memory capped at " + max + " KiB by heap size");
		}
		return max;
	}

	private long measureDuration(int cost) {
		long start = clock.currentTimeMillis();
		derive(new byte[16], new byte[32], cost);
		return clock.currentTimeMillis() - start;
	}

	@Override
	public SecretKey deriveKey(String password, byte[] salt, int cost) {
		long start = now();
		SecretKey k = new SecretKey(derive(StringUtils.toUtf8(password), salt,
				cost));
		logDuration(LOG, "Deriving key from password", start);
		return k;
	}

	private static byte[] derive(byte[] password, byte[] salt, int cost) {
		Argon2Parameters params =
				new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
						.withVersion(Argon2Parameters.ARGON2_VERSION_13)
						.withIterations(ITERATIONS)
						.withMemoryAsKB(cost)
						.withParallelism(PARALLELISM)
						.withSalt(salt)
						.build();
		Argon2BytesGenerator generator = new Argon2BytesGenerator();
		generator.init(params);
		byte[] out = new byte[SecretKey.LENGTH];
		generator.generateBytes(password, out);
		return out;
	}
}
