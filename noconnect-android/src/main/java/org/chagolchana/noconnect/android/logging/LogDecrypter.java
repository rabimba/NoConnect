package org.chagolchana.noconnect.android.logging;

import org.chagolchana.chagol.util.AndroidUtils;
import org.chagolchana.nullsafety.NotNullByDefault;

import androidx.annotation.Nullable;

@NotNullByDefault
public interface LogDecrypter {
	/**
	 * Returns decrypted log records from {@link AndroidUtils#getLogcatFile}
	 * or null if there was an error reading the logs.
	 */
	@Nullable
	String decryptLogs(@Nullable byte[] logKey);
}
