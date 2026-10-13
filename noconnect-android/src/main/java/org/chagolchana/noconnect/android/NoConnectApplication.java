package org.chagolchana.noconnect.android;

import android.app.Activity;
import android.content.SharedPreferences;

import org.chagolchana.chagol.ChagolApplication;
import org.chagolchana.noconnect.android.navdrawer.NavDrawerActivity;

/**
 * This exists so that the Application object will not necessarily be cast
 * directly to the NoConnect application object.
 */
public interface NoConnectApplication extends ChagolApplication {

	Class<? extends Activity> ENTRY_ACTIVITY = NavDrawerActivity.class;

	AndroidComponent getApplicationComponent();

	SharedPreferences getDefaultSharedPreferences();

	boolean isRunningInBackground();

	boolean isInstrumentationTest();
}
