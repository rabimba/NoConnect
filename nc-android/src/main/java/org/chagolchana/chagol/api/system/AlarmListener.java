package org.chagolchana.chagol.api.system;

import android.content.Intent;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AlarmListener {

	void onAlarm(Intent intent);
}
