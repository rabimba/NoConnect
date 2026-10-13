package org.briarproject.android.dontkillmelib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION.SDK_INT
import android.os.PowerManager
import android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED
import java.util.concurrent.atomic.AtomicBoolean

public abstract class AbstractDozeWatchdogImpl(private val appContext: Context) {

    private val dozed = AtomicBoolean(false)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SDK_INT < 23) return
            val pm = appContext.getSystemService(POWER_SERVICE) as PowerManager
            if (pm.isDeviceIdleMode) dozed.set(true)
        }
    }

    public val andResetDozeFlag: Boolean
        get() = dozed.getAndSet(false)

    public fun startService() {
        if (SDK_INT < 23) return
        val filter = IntentFilter(ACTION_DEVICE_IDLE_MODE_CHANGED)
        appContext.registerReceiver(receiver, filter)
    }

    public fun stopService() {
        if (SDK_INT < 23) return
        appContext.unregisterReceiver(receiver)
    }

}
