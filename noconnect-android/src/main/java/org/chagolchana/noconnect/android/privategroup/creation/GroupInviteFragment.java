package org.chagolchana.noconnect.android.privategroup.creation;

import android.os.Bundle;

import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.contactselection.ContactSelectorController;
import org.chagolchana.noconnect.android.contactselection.ContactSelectorFragment;
import org.chagolchana.noconnect.android.contactselection.SelectableContactItem;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import static org.chagolchana.noconnect.android.activity.NoConnectActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupInviteFragment extends ContactSelectorFragment {

	public static final String TAG = GroupInviteFragment.class.getName();

	@Inject
	CreateGroupController controller;

	public static GroupInviteFragment newInstance(GroupId groupId) {
		Bundle args = new Bundle();
		args.putByteArray(GROUP_ID, groupId.getBytes());
		GroupInviteFragment fragment = new GroupInviteFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().setTitle(R.string.groups_invite_members);
	}

	@Override
	protected ContactSelectorController<SelectableContactItem> getController() {
		return controller;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

}
