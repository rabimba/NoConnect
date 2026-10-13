package org.chagolchana.noconnect.android.navdrawer;

import org.junit.Test;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.chagolchana.noconnect.android.navdrawer.BuildAgePolicy.monthsOld;
import static org.chagolchana.noconnect.android.navdrawer.BuildAgePolicy.shouldShowReminder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildAgePolicyTest {

	private static final long BUILD = 1_700_000_000_000L;

	@Test
	public void testNoReminderForRecentBuild() {
		assertFalse(shouldShowReminder(BUILD + DAYS.toMillis(179), BUILD, 0));
	}

	@Test
	public void testReminderForOldBuildNeverDismissed() {
		assertTrue(shouldShowReminder(BUILD + DAYS.toMillis(180), BUILD, 0));
	}

	@Test
	public void testReminderWaitsThirtyDaysAfterDismissal() {
		long now = BUILD + DAYS.toMillis(200);
		assertFalse(shouldShowReminder(now, BUILD, now - DAYS.toMillis(29)));
		assertTrue(shouldShowReminder(now, BUILD, now - DAYS.toMillis(30)));
	}

	@Test
	public void testMonthsOld() {
		assertEquals(6, monthsOld(BUILD + DAYS.toMillis(185), BUILD));
	}
}
