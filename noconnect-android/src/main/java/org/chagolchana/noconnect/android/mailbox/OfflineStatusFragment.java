package org.chagolchana.noconnect.android.mailbox;

import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class OfflineStatusFragment extends OfflineFragment {

	public static final String TAG = OfflineStatusFragment.class.getName();

	@Override
	protected void onTryAgainClicked() {
		viewModel.checkIfOnlineWhenPaired();
	}

}
