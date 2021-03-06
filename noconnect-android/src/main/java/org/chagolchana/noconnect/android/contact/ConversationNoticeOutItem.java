package org.chagolchana.noconnect.android.contact;

import android.support.annotation.LayoutRes;

import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.noconnect.R;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class ConversationNoticeOutItem extends ConversationOutItem {

	@Nullable
	private final String msgText;

	ConversationNoticeOutItem(MessageId id, GroupId groupId,
			String text, @Nullable String msgText, long time,
			boolean sent, boolean seen) {
		super(id, groupId, text, time, sent, seen);
		this.msgText = msgText;
	}

	@Nullable
	String getMsgText() {
		return msgText;
	}

	@LayoutRes
	@Override
	public int getLayout() {
		return R.layout.list_item_conversation_notice_out;
	}

}
