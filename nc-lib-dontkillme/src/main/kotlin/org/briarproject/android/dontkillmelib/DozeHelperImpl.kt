package org.briarproject.android.dontkillmelib

import android.content.Context

public class DozeHelperImpl : DozeHelper {
    override fun needToShowDoNotKillMeFragment(context: Context): Boolean {
        val appContext = context.applicationContext
        return DozeUtils.needsDozeWhitelisting(appContext) ||
                HuaweiUtils.protectedAppsNeedsToBeShown(appContext) ||
                HuaweiUtils.appLaunchNeedsToBeShown(appContext) ||
                XiaomiUtils.xiaomiRecentAppsNeedsToBeShown() ||
                XiaomiUtils.xiaomiLockAppsNeedsToBeShown(appContext)
    }
}
