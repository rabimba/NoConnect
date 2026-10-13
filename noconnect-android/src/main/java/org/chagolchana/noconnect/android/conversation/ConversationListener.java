package org.chagolchana.noconnect.android.conversation;

import android.view.View;

import org.chagolchana.noconnect.android.attachment.AttachmentItem;
import org.chagolchana.nullsafety.NotNullByDefault;

import androidx.annotation.UiThread;

@UiThread
@NotNullByDefault
interface ConversationListener {

	void respondToRequest(ConversationRequestItem item, boolean accept);

	void openRequestedShareable(ConversationRequestItem item);

	void onAttachmentClicked(View view, ConversationMessageItem messageItem,
			AttachmentItem attachmentItem);

	void onAutoDeleteTimerNoticeClicked();

	void onLinkClick(String url);

}
