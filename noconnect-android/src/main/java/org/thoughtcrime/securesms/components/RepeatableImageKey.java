package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.support.v7.widget.AppCompatImageButton;
//import android.widget.ImageButton;

import static android.view.HapticFeedbackConstants.KEYBOARD_TAP;
import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

@UiThread
public class RepeatableImageKey extends AppCompatImageButton {

	private KeyEventListener listener;

	public RepeatableImageKey(Context context) {
		super(context);
		init();
	}

	public RepeatableImageKey(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RepeatableImageKey(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		setOnClickListener(new RepeaterClickListener());
		setOnTouchListener(new RepeaterTouchListener());
	}

	public void setOnKeyEventListener(KeyEventListener listener) {
		this.listener = listener;
	}

	private void notifyListener() {
		if (listener != null) listener.onKeyEvent();
	}

	private class RepeaterClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			notifyListener();
		}
	}

	private class Repeater implements Runnable {
		@Override
		public void run() {
			notifyListener();
			postDelayed(this, ViewConfiguration.getKeyRepeatDelay());
		}
	}

	private class RepeaterTouchListener implements OnTouchListener {

		private final Repeater repeater;

		private RepeaterTouchListener() {
			repeater = new Repeater();
		}

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
				case ACTION_DOWN:
					view.postDelayed(repeater,
							ViewConfiguration.getKeyRepeatTimeout());
					performHapticFeedback(KEYBOARD_TAP);
					return false;
				case ACTION_CANCEL:
				case ACTION_UP:
					view.removeCallbacks(repeater);
					return false;
				default:
					return false;
			}
		}
	}

	public interface KeyEventListener {
		void onKeyEvent();
	}
}
