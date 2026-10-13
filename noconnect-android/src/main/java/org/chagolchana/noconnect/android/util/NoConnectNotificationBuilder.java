package org.chagolchana.noconnect.android.util;

import android.content.Context;

import org.chagolchana.noconnect.R;

import androidx.annotation.ColorRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE;

public class NoConnectNotificationBuilder extends NotificationCompat.Builder {

	private final Context context;

	public NoConnectNotificationBuilder(Context context, String channelId) {
		super(context, channelId);
		this.context = context;
		// Auto-cancel does not fire the delete intent, see
		// https://issuetracker.google.com/issues/36961721
		setAutoCancel(true);

		setLights(ContextCompat.getColor(context, R.color.nc_lime_400),
				750, 500);
		setVisibility(VISIBILITY_PRIVATE);
	}

	public NoConnectNotificationBuilder setColorRes(@ColorRes int res) {
		setColor(ContextCompat.getColor(context, res));
		return this;
	}

	public NoConnectNotificationBuilder setNotificationCategory(String category) {
		setCategory(category);
		return this;
	}

}
