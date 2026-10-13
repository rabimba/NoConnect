package org.chagolchana.noconnect.android.forum;

import org.chagolchana.noconnect.android.threaded.ThreadItem;
import org.chagolchana.noconnect.api.forum.ForumPostHeader;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
class ForumPostItem extends ThreadItem {

	ForumPostItem(ForumPostHeader h, String text) {
		super(h.getId(), h.getParentId(), text, h.getTimestamp(), h.getAuthor(),
				h.getAuthorInfo(), h.isRead());
	}

}
