package org.chagolchana.noconnect.android.privategroup.memberlist;

import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.view.AuthorView;

import static org.chagolchana.chagol.api.identity.Author.Status.OURSELVES;

@UiThread
@NotNullByDefault
class MemberListItemHolder extends RecyclerView.ViewHolder {

	private final AuthorView author;
	private final ImageView bulb;
	private final TextView creator;

	MemberListItemHolder(View v) {
		super(v);
		author = (AuthorView) v.findViewById(R.id.authorView);
		bulb = (ImageView) v.findViewById(R.id.bulbView);
		creator = (TextView) v.findViewById(R.id.creatorView);
	}

	protected void bind(MemberListItem item) {
		// member name, avatar and status
		author.setAuthor(item.getMember());
		author.setAuthorStatus(item.getStatus());

		// online status of visible contacts
		if (item.getContactId() != null) {
			bulb.setVisibility(View.VISIBLE);
			if (item.isOnline()) {
				bulb.setImageResource(R.drawable.contact_connected);
			} else {
				bulb.setImageResource(R.drawable.contact_disconnected);
			}
		} else {
			bulb.setVisibility(View.GONE);
		}

		// text shown for creator
		if (item.isCreator()) {
			creator.setVisibility(View.VISIBLE);
			if (item.getStatus() == OURSELVES) {
				creator.setText(R.string.groups_member_created_you);
			} else {
				creator.setText(creator.getContext()
						.getString(R.string.groups_member_created,
								item.getMember().getName()));
			}
		} else {
			creator.setVisibility(View.GONE);
		}
	}

}
