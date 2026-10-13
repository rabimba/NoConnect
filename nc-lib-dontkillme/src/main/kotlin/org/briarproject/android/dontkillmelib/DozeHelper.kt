package org.briarproject.android.dontkillmelib

import android.content.Context

public interface DozeHelper {
    public fun needToShowDoNotKillMeFragment(context: Context): Boolean
}
