package org.chagolchana.noconnect.android.conversation;

import android.view.View;
import android.widget.ImageView;

import org.chagolchana.noconnect.R;
import org.chagolchana.nullsafety.NotNullByDefault;

import androidx.annotation.UiThread;

@UiThread
@NotNullByDefault
class OutItemViewHolder {

	private final ImageView status;

	OutItemViewHolder(View v) {
		status = v.findViewById(R.id.status);
	}

	void bind(ConversationItem item) {
		int res;
		if (item.isSeen()) {
			res = R.drawable.message_delivered;
		} else if (item.isSent()) {
			res = R.drawable.message_sent;
		} else {
			res = R.drawable.message_stored;
		}
		status.setImageResource(res);
	}

}
