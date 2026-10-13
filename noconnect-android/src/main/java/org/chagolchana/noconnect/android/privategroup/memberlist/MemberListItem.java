package org.chagolchana.noconnect.android.privategroup.memberlist;

import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.noconnect.api.identity.AuthorInfo.Status;
import org.chagolchana.noconnect.api.privategroup.GroupMember;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class MemberListItem {

	private final GroupMember groupMember;
	private boolean online;

	MemberListItem(GroupMember groupMember, boolean online) {
		this.groupMember = groupMember;
		this.online = online;
	}

	Author getMember() {
		return groupMember.getAuthor();
	}

	AuthorInfo getAuthorInfo() {
		return groupMember.getAuthorInfo();
	}

	Status getStatus() {
		return groupMember.getAuthorInfo().getStatus();
	}

	boolean isCreator() {
		return groupMember.isCreator();
	}

	@Nullable
	ContactId getContactId() {
		return groupMember.getContactId();
	}

	boolean isOnline() {
		return online;
	}

	void setOnline(boolean online) {
		this.online = online;
	}

}
