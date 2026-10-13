package org.chagolchana.noconnect.android.privategroup.invitation;

import android.content.Context;

import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.activity.ActivityComponent;
import org.chagolchana.noconnect.android.sharing.InvitationActivity;
import org.chagolchana.noconnect.android.sharing.InvitationAdapter;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationItem;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

import static org.chagolchana.noconnect.android.sharing.InvitationAdapter.InvitationClickListener;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupInvitationActivity
		extends InvitationActivity<GroupInvitationItem> {

	@Inject
	protected GroupInvitationController controller;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	protected GroupInvitationController getController() {
		return controller;
	}

	@Override
	protected InvitationAdapter<GroupInvitationItem, ?> getAdapter(Context ctx,
			InvitationClickListener<GroupInvitationItem> listener) {
		return new GroupInvitationAdapter(ctx, listener);
	}

	@Override
	protected int getAcceptRes() {
		return R.string.groups_invitations_joined;
	}

	@Override
	protected int getDeclineRes() {
		return R.string.groups_invitations_declined;
	}

}
