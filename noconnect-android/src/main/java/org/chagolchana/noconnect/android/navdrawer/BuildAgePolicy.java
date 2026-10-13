package org.chagolchana.noconnect.android.navdrawer;

import org.chagolchana.nullsafety.NotNullByDefault;

import static java.util.concurrent.TimeUnit.DAYS;

/**
 * Decides when to remind the user that their build is old. NoConnect is
 * often installed from another phone rather than an app store, so nothing
 * else tells people a newer version with security fixes exists.
 */
@NotNullByDefault
class BuildAgePolicy {

	/**
	 * Builds older than this get a reminder.
	 */
	static final long REMINDER_AGE_MS = DAYS.toMillis(180);

	/**
	 * After the reminder is dismissed, wait this long before showing it again.
	 */
	static final long REMINDER_INTERVAL_MS = DAYS.toMillis(30);

	/**
	 * @param lastDismissed when the reminder was last dismissed, or 0 if never
	 */
	static boolean shouldShowReminder(long now, long buildTime,
			long lastDismissed) {
		if (now - buildTime < REMINDER_AGE_MS) return false;
		return lastDismissed == 0 || now - lastDismissed >= REMINDER_INTERVAL_MS;
	}

	static int monthsOld(long now, long buildTime) {
		return (int) ((now - buildTime) / DAYS.toMillis(30));
	}
}
