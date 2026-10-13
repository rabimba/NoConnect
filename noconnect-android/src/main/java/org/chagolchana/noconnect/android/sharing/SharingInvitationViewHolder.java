package org.chagolchana.noconnect.android.sharing;

import android.view.View;

import org.chagolchana.chagol.api.contact.Contact;
import org.chagolchana.chagol.util.StringUtils;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.sharing.InvitationAdapter.InvitationClickListener;
import org.chagolchana.noconnect.api.sharing.SharingInvitationItem;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import static org.chagolchana.noconnect.android.util.UiUtils.getContactDisplayName;

class SharingInvitationViewHolder
		extends InvitationViewHolder<SharingInvitationItem> {

	SharingInvitationViewHolder(View v) {
		super(v);
	}

	@Override
	public void onBind(@Nullable SharingInvitationItem item,
			InvitationClickListener<SharingInvitationItem> listener) {
		super.onBind(item, listener);
		if (item == null) return;

		Collection<String> names = new ArrayList<>();
		for (Contact c : item.getNewSharers())
			names.add(getContactDisplayName(c));
		sharedBy.setText(
				sharedBy.getContext().getString(R.string.shared_by_format,
						StringUtils.join(names, ", ")));
	}

}
