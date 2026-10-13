package org.briarproject.android.dontkillmelib

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.os.Build.BRAND
import android.os.Build.VERSION.INCREMENTAL
import android.util.Log

public object XiaomiUtils {

    private const val TAG = "XiaomiUtils"

    private const val PACKAGE_NAME = "com.miui.securitycenter"
    private const val CLASS_NAME = "com.miui.securityscan.ui.settings.SettingsActivity"

    @JvmStatic
    public val isXiaomiOrRedmiDevice: Boolean
        get() = "Xiaomi".equals(BRAND, ignoreCase = true) ||
                "Redmi".equals(BRAND, ignoreCase = true)

    @JvmStatic
    public val isMiuiTenOrLater: Boolean
        get() = isMiuiVersionAtLeast(10, 0)

    @JvmStatic
    public fun isMiuiVersionAtLeast(major: Int, minor: Int): Boolean {
        // E.g. "V12.5.3.0.QFGMIXM"
        val version = INCREMENTAL
        Log.i(TAG, "MIUI version $version")
        return try {
            val pair = parseMiuiVersion(version)
            val maj = pair.first
            val min = pair.second
            Log.i(TAG, "Parsed version $maj.$min")
            maj > major || (maj == major && min >= minor)
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse MIUI version")
            false
        }
    }

    public fun parseMiuiVersion(version: String): Pair<Int, Int> {
        val parts = version.replace("[^\\d.]".toRegex(), "").split(".")
        if (parts.size < 2) throw IllegalArgumentException()
        return Pair(parts[0].toInt(), parts[1].toInt())
    }

    @JvmStatic
    public fun xiaomiRecentAppsNeedsToBeShown(): Boolean =
        isXiaomiOrRedmiDevice && !isMiuiVersionAtLeast(12, 5)

    @JvmStatic
    public fun xiaomiLockAppsNeedsToBeShown(context: Context): Boolean {
        if (!isXiaomiOrRedmiDevice || !isMiuiVersionAtLeast(12, 5)) return false
        val pm = context.packageManager
        val resolveInfos = pm.queryIntentActivities(xiaomiLockAppsIntent, MATCH_DEFAULT_ONLY)
        return resolveInfos.isNotEmpty()
    }

    @JvmStatic
    public val xiaomiLockAppsIntent: Intent
        get() = Intent().apply {
            setClassName(PACKAGE_NAME, CLASS_NAME)
        }
}
