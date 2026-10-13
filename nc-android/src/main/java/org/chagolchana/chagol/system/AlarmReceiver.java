package org.chagolchana.chagol.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.chagolchana.chagol.ChagolApplication;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		ChagolApplication app =
				(ChagolApplication) ctx.getApplicationContext();
		app.getChagolAppComponent().alarmListener().onAlarm(intent);
	}
}
