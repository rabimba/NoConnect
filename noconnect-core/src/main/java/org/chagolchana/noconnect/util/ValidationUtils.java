package org.chagolchana.noconnect.util;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static org.chagolchana.chagol.util.ValidationUtils.checkRange;
import static org.chagolchana.noconnect.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static org.chagolchana.noconnect.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.chagolchana.noconnect.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;

@Immutable
@NotNullByDefault
public class ValidationUtils {

	public static long validateAutoDeleteTimer(@Nullable Long timer)
			throws FormatException {
		if (timer == null) return NO_AUTO_DELETE_TIMER;
		checkRange(timer, MIN_AUTO_DELETE_TIMER_MS, MAX_AUTO_DELETE_TIMER_MS);
		return timer;
	}
}
