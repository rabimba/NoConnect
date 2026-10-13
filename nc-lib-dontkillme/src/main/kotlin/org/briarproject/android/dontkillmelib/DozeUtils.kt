package org.briarproject.android.dontkillmelib

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

public object DozeUtils {

    @JvmStatic
    public fun needsDozeWhitelisting(ctx: Context): Boolean {
        if (SDK_INT < 23) return false
        val pm = ctx.getSystemService(POWER_SERVICE) as PowerManager
        return !pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    @JvmStatic
    @TargetApi(23)
    @SuppressLint("BatteryLife")
    public fun getDozeWhitelistingIntent(ctx: Context): Intent = Intent().apply {
        action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        data = Uri.parse("package:" + ctx.packageName)
    }

}
