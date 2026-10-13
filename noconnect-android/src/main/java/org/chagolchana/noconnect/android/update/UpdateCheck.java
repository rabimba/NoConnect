package org.chagolchana.noconnect.android.update;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * Decides whether an APK received from someone else may be installed as an
 * update. Android itself refuses an update signed with a different key; these
 * checks let the app explain the problem before handing the file to the
 * system installer, and stop downgrades to older, possibly vulnerable builds.
 */
@NotNullByDefault
class UpdateCheck {

	enum Result {
		OK, NOT_AN_APK, WRONG_APP, NOT_NEWER, WRONG_SIGNER
	}

	/**
	 * @param apkPackage the APK's package name, or null if it couldn't be read
	 * @param apkSigners SHA-256 digests of the APK's signing certificates
	 * @param ourSigners SHA-256 digests of the installed app's signing
	 * certificates
	 */
	static Result check(@Nullable String apkPackage, long apkVersionCode,
			Set<String> apkSigners, String ourPackage, long ourVersionCode,
			Set<String> ourSigners) {
		if (apkPackage == null) return Result.NOT_AN_APK;
		if (!apkPackage.equals(ourPackage)) return Result.WRONG_APP;
		if (apkSigners.isEmpty() || !apkSigners.equals(ourSigners)) {
			return Result.WRONG_SIGNER;
		}
		if (apkVersionCode <= ourVersionCode) return Result.NOT_NEWER;
		return Result.OK;
	}
}
