package org.chagolchana.noconnect.android.logging;

import org.chagolchana.chagol.util.AndroidUtils;
import org.chagolchana.nullsafety.NotNullByDefault;

import androidx.annotation.Nullable;

@NotNullByDefault
public interface LogEncrypter {
	/**
	 * Writes encrypted log records to {@link AndroidUtils#getLogcatFile}
	 * and returns the encryption key if everything went fine.
	 */
	@Nullable
	byte[] encryptLogs();
}
