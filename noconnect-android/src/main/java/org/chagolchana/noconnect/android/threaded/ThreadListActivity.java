package org.chagolchana.noconnect.android.threaded;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.NoConnectActivity;
import org.chagolchana.noconnect.android.sharing.SharingController.SharingInfo;
import org.chagolchana.noconnect.android.threaded.ThreadItemAdapter.ThreadItemListener;
import org.chagolchana.noconnect.android.util.NoConnectSnackbarBuilder;
import org.chagolchana.noconnect.android.view.NoConnectRecyclerView;
import org.chagolchana.noconnect.android.view.TextInputView;
import org.chagolchana.noconnect.android.view.TextSendController;
import org.chagolchana.noconnect.android.view.TextSendController.SendListener;
import org.chagolchana.noconnect.android.view.TextSendController.SendState;
import org.chagolchana.noconnect.android.view.UnreadMessageButton;
import org.chagolchana.noconnect.android.widget.LinkDialogFragment;
import org.chagolchana.noconnect.api.attachment.AttachmentHeader;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.util.List;

import javax.annotation.Nullable;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.CallSuper;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.chagolchana.chagol.util.StringUtils.isNullOrEmpty;
import static org.chagolchana.noconnect.android.view.TextSendController.SendState.SENT;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ThreadListActivity<I extends ThreadItem, A extends ThreadItemAdapter<I>>
		extends NoConnectActivity implements SendListener, ThreadItemListener<I> {

	protected final A adapter = createAdapter();
	protected abstract ThreadListViewModel<I> getViewModel();
	protected abstract A createAdapter();
	protected NoConnectRecyclerView list;
	protected TextInputView textInput;
	protected TextSendController sendController;
	protected GroupId groupId;

	private LinearLayoutManager layoutManager;
	private ThreadScrollListener<I> scrollListener;

	@CallSuper
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_threaded_conversation);

		Intent i = getIntent();
		byte[] b = i.getByteArrayExtra(GROUP_ID);
		if (b == null) throw new IllegalStateException("No GroupId in intent.");
		groupId = new GroupId(b);
		ThreadListViewModel<I> viewModel = getViewModel();
		viewModel.setGroupId(groupId);

		textInput = findViewById(R.id.text_input_container);
		sendController = new TextSendController(textInput, this, false);
		textInput.setSendController(sendController);
		textInput.setMaxTextLength(getMaxTextLength());
		textInput.setReady(true);

		UnreadMessageButton upButton = findViewById(R.id.upButton);
		UnreadMessageButton downButton = findViewById(R.id.downButton);

		list = findViewById(R.id.list);
		layoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(layoutManager);
		list.setAdapter(adapter);
		scrollListener = new ThreadScrollListener<>(adapter, viewModel,
				upButton, downButton);
		list.getRecyclerView().addOnScrollListener(scrollListener);

		upButton.setOnClickListener(v -> {
			int position = adapter.getVisibleUnreadPosTop(layoutManager);
			if (position != NO_POSITION) {
				list.getRecyclerView().scrollToPosition(position);
			}
		});
		downButton.setOnClickListener(v -> {
			int position = adapter.getVisibleUnreadPosBottom(layoutManager);
			if (position != NO_POSITION) {
				list.getRecyclerView().scrollToPosition(position);
			}
		});

		viewModel.getItems().observe(this, result -> result
				.onError(this::handleException)
				.onSuccess(this::displayItems)
		);

		viewModel.getSharingInfo().observe(this, this::setToolbarSubTitle);

		viewModel.getGroupRemoved().observe(this, removed -> {
			if (removed) supportFinishAfterTransition();
		});

		getOnBackPressedDispatcher().addCallback(this,
				new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {
						if (adapter.getHighlightedItem() != null) {
							textInput.clearText();
							getViewModel().setReplyId(null);
							updateTextInput();
						} else {
							setEnabled(false);
							getOnBackPressedDispatcher().onBackPressed();
							setEnabled(true);
						}
					}
				});
	}

	@CallSuper
	@Override
	public void onStart() {
		super.onStart();
		getViewModel().blockAndClearNotifications();
		list.startPeriodicUpdate();
	}

	@CallSuper
	@Override
	public void onStop() {
		super.onStop();
		getViewModel().unblockNotifications();
		list.stopPeriodicUpdate();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			supportFinishAfterTransition();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// store list position, so we can restore it when coming back here
		if (layoutManager != null && adapter != null) {
			MessageId id = adapter.getFirstVisibleMessageId(layoutManager);
			getViewModel().storeMessageId(id);
		}
	}

	protected void displayItems(List<I> items) {
		if (items.isEmpty()) {
			list.showData();
		} else {
			adapter.submitList(items, () -> {
				// do stuff *after* list had been updated
				scrollAfterListCommitted();
				updateTextInput();
			});
		}
	}

	/**
	 * Scrolls to the first visible item last time the activity was open,
	 * if one exists and this is the first time, the list gets displayed.
	 * Or scrolls to a locally added item that has just been added to the list.
	 */
	private void scrollAfterListCommitted() {
		MessageId restoredFirstVisibleItemId =
				getViewModel().getAndResetRestoredMessageId();
		MessageId scrollToItem =
				getViewModel().getAndResetScrollToItem();
		if (restoredFirstVisibleItemId != null) {
			scrollToItemAtTop(restoredFirstVisibleItemId);
		} else if (scrollToItem != null) {
			scrollToItemAtTop(scrollToItem);
		}
		scrollListener.updateUnreadButtons(layoutManager);
	}

	@Override
	public void onReplyClick(I item) {
		getViewModel().setReplyId(item.getId());
		updateTextInput();
		// FIXME This does not work for a hardware keyboard
		if (textInput.isKeyboardOpen()) {
			scrollToItemAtTop(item.getId());
		} else {
			// wait with scrolling until keyboard opened
			textInput.setOnKeyboardShownListener(() -> {
				scrollToItemAtTop(item.getId());
				textInput.setOnKeyboardShownListener(null);
			});
		}
	}

	@Override
	public void onLinkClick(String url) {
		LinkDialogFragment f = LinkDialogFragment.newInstance(url);
		f.show(getSupportFragmentManager(), f.getUniqueTag());
	}

	protected void setToolbarSubTitle(SharingInfo sharingInfo) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setSubtitle(getString(R.string.shared_with,
					sharingInfo.total, sharingInfo.online));
		}
	}

	private void scrollToItemAtTop(MessageId messageId) {
		int position = adapter.findItemPosition(messageId);
		if (position != NO_POSITION) {
			layoutManager.scrollToPositionWithOffset(position, 0);
		}
	}

	protected void displaySnackbar(@StringRes int stringId) {
		new NoConnectSnackbarBuilder()
				.make(list, stringId, Snackbar.LENGTH_SHORT)
				.show();
	}

	private void updateTextInput() {
		MessageId replyId = getViewModel().getReplyId();
		if (replyId != null) {
			textInput.setHint(R.string.forum_message_reply_hint);
			textInput.showSoftKeyboard();
		} else {
			textInput.setHint(R.string.forum_new_message_hint);
		}
		adapter.setHighlightedItem(replyId);
	}

	@Override
	public LiveData<SendState> onSendClick(@Nullable String text,
			List<AttachmentHeader> headers, long expectedAutoDeleteTimer) {
		if (isNullOrEmpty(text)) throw new AssertionError();

		MessageId replyId = getViewModel().getReplyId();
		getViewModel().createAndStoreMessage(text, replyId);
		textInput.hideSoftKeyboard();
		textInput.clearText();
		getViewModel().setReplyId(null);
		updateTextInput();
		return new MutableLiveData<>(SENT);
	}

	protected abstract int getMaxTextLength();

}
