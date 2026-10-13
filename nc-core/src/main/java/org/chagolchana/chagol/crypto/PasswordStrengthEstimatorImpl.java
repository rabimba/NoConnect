package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

/**
 * Estimates password strength from an entropy approximation: each character
 * contributes log2 of the size of the character classes the password uses,
 * except repeats and runs (such as "aaa", "abc" or "321"), which contribute
 * almost nothing. Very common passwords score close to zero. {@link #STRONG}
 * corresponds to {@link #STRONG_BITS} bits.
 */
@Immutable
@NotNullByDefault
class PasswordStrengthEstimatorImpl implements PasswordStrengthEstimator {

	static final double STRONG_BITS = 60;

	// Bits for a character that repeats or continues a run
	private static final double PATTERN_CHAR_BITS = 0.2;

	private static final Set<String> COMMON = new HashSet<>(Arrays.asList(
			"password", "passw0rd", "p@ssw0rd", "p@ssword", "letmein",
			"welcome", "qwerty", "qwertyuiop", "asdfghjkl", "zxcvbnm",
			"iloveyou", "admin", "administrator", "login", "master",
			"monkey", "dragon", "sunshine", "princess", "football",
			"baseball", "superman", "batman", "trustno1", "shadow",
			"michael", "jennifer", "charlie", "secret", "starwars",
			"whatever", "freedom", "hello", "loveme", "abc123",
			"changeme", "default", "access", "computer", "internet",
			"android", "samsung", "google", "facebook", "pokemon"));

	@Override
	public float estimateStrength(String password) {
		if (password.isEmpty()) return NONE;
		if (isCommon(password)) return WEAK / 2;
		double perChar = log2(poolSize(password));
		double bits = 0;
		int prev = -1, prevDelta = 0;
		for (int i = 0; i < password.length(); ) {
			int c = password.codePointAt(i);
			int delta = prev == -1 ? 0 : c - prev;
			boolean repeat = prev != -1 && delta == 0;
			boolean run = prev != -1 && Math.abs(delta) == 1
					&& (delta == prevDelta || prevDelta == 0);
			bits += repeat || run ? PATTERN_CHAR_BITS : perChar;
			prevDelta = delta;
			prev = c;
			i += Character.charCount(c);
		}
		return (float) Math.min(1, bits / STRONG_BITS);
	}

	/**
	 * Returns true if the password, ignoring case and any trailing digits
	 * and symbols (as in "Password123!"), is a very common password.
	 */
	private static boolean isCommon(String password) {
		String lower = password.toLowerCase(Locale.ROOT);
		if (COMMON.contains(lower)) return true;
		String stem = lower.replaceAll("[^a-z]+$", "");
		return !stem.isEmpty() && COMMON.contains(stem);
	}

	private static int poolSize(String password) {
		boolean lower = false, upper = false, digit = false, symbol = false,
				other = false;
		for (int i = 0; i < password.length(); ) {
			int c = password.codePointAt(i);
			if (c >= 'a' && c <= 'z') lower = true;
			else if (c >= 'A' && c <= 'Z') upper = true;
			else if (c >= '0' && c <= '9') digit = true;
			else if (c < 128) symbol = true;
			else other = true;
			i += Character.charCount(c);
		}
		int pool = 0;
		if (lower) pool += 26;
		if (upper) pool += 26;
		if (digit) pool += 10;
		if (symbol) pool += 33;
		if (other) pool += 100;
		return pool;
	}

	private static double log2(int x) {
		return Math.log(x) / Math.log(2);
	}
}
