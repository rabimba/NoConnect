package org.chagolchana.chagol.crypto;

import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import static org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator.NONE;
import static org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator.QUITE_STRONG;
import static org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator.STRONG;
import static org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator.WEAK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PasswordStrengthEstimatorImplTest extends ChagolTestCase {

	private final PasswordStrengthEstimator e =
			new PasswordStrengthEstimatorImpl();

	@Test
	public void testWeakPasswords() {
		assertTrue(e.estimateStrength("") == NONE);
		assertTrue(e.estimateStrength("password") < QUITE_STRONG);
		assertTrue(e.estimateStrength("letmein") < QUITE_STRONG);
		assertTrue(e.estimateStrength("123456") < QUITE_STRONG);
	}

	@Test
	public void testStrongPasswords() {
		// Industry standard
		assertTrue(e.estimateStrength("Tr0ub4dor&3") > QUITE_STRONG);
		assertTrue(e.estimateStrength("correcthorsebatterystaple")
				> QUITE_STRONG);
	}

	@Test
	public void testCommonPasswordsWithDecorationsAreWeak() {
		assertTrue(e.estimateStrength("Password123!") < WEAK);
		assertTrue(e.estimateStrength("QWERTY") < WEAK);
		assertTrue(e.estimateStrength("iloveyou2026") < WEAK);
	}

	@Test
	public void testRepeatsAndRunsAreWeak() {
		// Six distinct characters were enough for the old estimator
		assertTrue(e.estimateStrength("abcdef") < QUITE_STRONG);
		assertTrue(e.estimateStrength("aaaaaaaaaaaaaaaaaaaa") < WEAK);
		assertTrue(e.estimateStrength("abcdefghijklmnopqrst") < WEAK);
		assertTrue(e.estimateStrength("9876543210") < WEAK);
	}

	@Test
	public void testLengthAndCharacterClasses() {
		// 8 random lowercase letters: about 38 bits
		assertTrue(e.estimateStrength("qzmvbrtx") < QUITE_STRONG);
		// 10 random lowercase letters: about 47 bits
		assertTrue(e.estimateStrength("qzmvbrtxpw") >= QUITE_STRONG);
		// 8 characters from all classes: about 53 bits
		assertTrue(e.estimateStrength("k7#Rw2!q") >= QUITE_STRONG);
	}

	@Test
	public void testNonAsciiPassphrasesCount() {
		assertTrue(e.estimateStrength("кошка собака дом") >= QUITE_STRONG);
	}

	@Test
	public void testStrengthIsCappedAtStrong() {
		assertEquals(STRONG, e.estimateStrength(
				"a very long passphrase made of many ordinary words"), 0);
	}
}
