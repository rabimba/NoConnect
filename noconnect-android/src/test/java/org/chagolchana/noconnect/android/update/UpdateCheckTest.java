package org.chagolchana.noconnect.android.update;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.chagolchana.noconnect.android.update.UpdateCheck.Result.NOT_AN_APK;
import static org.chagolchana.noconnect.android.update.UpdateCheck.Result.NOT_NEWER;
import static org.chagolchana.noconnect.android.update.UpdateCheck.Result.OK;
import static org.chagolchana.noconnect.android.update.UpdateCheck.Result.WRONG_APP;
import static org.chagolchana.noconnect.android.update.UpdateCheck.Result.WRONG_SIGNER;
import static org.chagolchana.noconnect.android.update.UpdateCheck.check;
import static org.junit.Assert.assertEquals;

public class UpdateCheckTest {

	private static final String PKG = "org.chagolchana.noconnect.android";
	private static final Set<String> SIGNERS = singleton("aa11");

	@Test
	public void testNewerVersionFromSameSignerIsAccepted() {
		assertEquals(OK, check(PKG, 11, SIGNERS, PKG, 10, SIGNERS));
	}

	@Test
	public void testUnreadableFileIsRejected() {
		assertEquals(NOT_AN_APK, check(null, 0, SIGNERS, PKG, 10, SIGNERS));
	}

	@Test
	public void testOtherAppIsRejected() {
		assertEquals(WRONG_APP,
				check("com.example.other", 11, SIGNERS, PKG, 10, SIGNERS));
	}

	@Test
	public void testDifferentSignerIsRejected() {
		assertEquals(WRONG_SIGNER,
				check(PKG, 11, singleton("bb22"), PKG, 10, SIGNERS));
	}

	@Test
	public void testExtraOrMissingSignersAreRejected() {
		Set<String> two = new HashSet<>(asList("aa11", "bb22"));
		assertEquals(WRONG_SIGNER, check(PKG, 11, two, PKG, 10, SIGNERS));
		assertEquals(WRONG_SIGNER, check(PKG, 11, Collections.emptySet(),
				PKG, 10, SIGNERS));
	}

	@Test
	public void testSameOrOlderVersionIsRejected() {
		assertEquals(NOT_NEWER, check(PKG, 10, SIGNERS, PKG, 10, SIGNERS));
		assertEquals(NOT_NEWER, check(PKG, 9, SIGNERS, PKG, 10, SIGNERS));
	}
}
