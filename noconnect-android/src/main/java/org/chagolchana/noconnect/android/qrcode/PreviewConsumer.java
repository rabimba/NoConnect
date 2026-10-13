package org.chagolchana.noconnect.android.qrcode;

import android.hardware.Camera;

import org.chagolchana.nullsafety.NotNullByDefault;

import androidx.annotation.UiThread;

@NotNullByDefault
public interface PreviewConsumer {

	@UiThread
	void start(Camera camera, int cameraIndex);

	@UiThread
	void stop();
}
