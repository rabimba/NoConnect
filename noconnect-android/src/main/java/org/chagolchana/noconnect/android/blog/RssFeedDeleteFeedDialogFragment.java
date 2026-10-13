package org.chagolchana.noconnect.android.blog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.BaseActivity;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import static java.util.Objects.requireNonNull;
import static org.chagolchana.noconnect.android.activity.NoConnectActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class RssFeedDeleteFeedDialogFragment extends DialogFragment {
	final static String TAG = RssFeedDeleteFeedDialogFragment.class.getName();

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private RssFeedViewModel viewModel;

	static RssFeedDeleteFeedDialogFragment newInstance(GroupId groupId) {
		Bundle args = new Bundle();
		args.putByteArray(GROUP_ID, groupId.getBytes());
		RssFeedDeleteFeedDialogFragment f =
				new RssFeedDeleteFeedDialogFragment();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Context ctx) {
		super.onAttach(ctx);
		((BaseActivity) requireActivity()).getActivityComponent().inject(this);

		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(RssFeedViewModel.class);
	}

	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		GroupId groupId = new GroupId(
				requireNonNull(requireArguments().getByteArray(GROUP_ID)));
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
				requireActivity(), R.style.NoConnectDialogTheme);
		builder.setTitle(getString(R.string.blogs_rss_remove_feed));
		builder.setMessage(
				getString(R.string.blogs_rss_remove_feed_dialog_message));
		builder.setPositiveButton(R.string.cancel, null);
		builder.setNegativeButton(R.string.blogs_rss_remove_feed_ok,
				(dialog, which) -> viewModel.removeFeed(groupId));
		return builder.create();
	}
}
