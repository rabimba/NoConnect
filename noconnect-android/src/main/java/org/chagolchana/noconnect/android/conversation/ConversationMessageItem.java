package org.chagolchana.noconnect.android.conversation;

import org.chagolchana.noconnect.android.attachment.AttachmentItem;
import org.chagolchana.noconnect.api.messaging.PrivateMessageHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;

@NotThreadSafe
@NotNullByDefault
class ConversationMessageItem extends ConversationItem {

	private final List<AttachmentItem> attachments;

	ConversationMessageItem(@LayoutRes int layoutRes, PrivateMessageHeader h,
			LiveData<String> contactName, List<AttachmentItem> attachments) {
		super(layoutRes, h, contactName);
		this.attachments = attachments;
	}

	List<AttachmentItem> getAttachments() {
		return attachments;
	}

	@UiThread
	boolean updateAttachments(AttachmentItem item) {
		int pos = attachments.indexOf(item);
		if (pos != -1 && attachments.get(pos).getState() != item.getState()) {
			attachments.set(pos, item);
			return true;
		}
		return false;
	}

}
