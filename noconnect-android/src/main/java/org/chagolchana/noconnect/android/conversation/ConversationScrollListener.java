package org.chagolchana.noconnect.android.conversation;

import org.chagolchana.noconnect.android.view.NoConnectRecyclerViewScrollListener;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
class ConversationScrollListener extends
		NoConnectRecyclerViewScrollListener<ConversationAdapter, ConversationItem> {

	private final ConversationViewModel viewModel;

	protected ConversationScrollListener(ConversationAdapter adapter,
			ConversationViewModel viewModel) {
		super(adapter);
		this.viewModel = viewModel;
	}

	@Override
	protected void onItemVisible(ConversationItem item) {
		if (!item.isRead()) {
			viewModel.markMessageRead(item.getGroupId(), item.getId());
			item.markRead();
		}
	}

}
