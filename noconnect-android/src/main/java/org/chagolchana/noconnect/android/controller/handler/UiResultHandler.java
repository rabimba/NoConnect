package org.chagolchana.noconnect.android.controller.handler;

import org.chagolchana.noconnect.android.DestroyableContext;

import androidx.annotation.UiThread;

public abstract class UiResultHandler<R> implements ResultHandler<R> {

	private final DestroyableContext listener;

	protected UiResultHandler(DestroyableContext listener) {
		this.listener = listener;
	}

	@Override
	public void onResult(R result) {
		listener.runOnUiThreadUnlessDestroyed(() -> onResultUi(result));
	}

	@UiThread
	public abstract void onResultUi(R result);
}
