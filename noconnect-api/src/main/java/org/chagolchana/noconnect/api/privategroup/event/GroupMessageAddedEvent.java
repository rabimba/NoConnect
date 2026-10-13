package org.chagolchana.noconnect.api.privategroup.event;

import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.noconnect.api.privategroup.GroupMessageHeader;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a private group message was added
 * to the database.
 */
@Immutable
@NotNullByDefault
public class GroupMessageAddedEvent extends Event {

	private final GroupId groupId;
	private final GroupMessageHeader header;
	private final String text;
	private final boolean local;

	public GroupMessageAddedEvent(GroupId groupId, GroupMessageHeader header,
			String text, boolean local) {
		this.groupId = groupId;
		this.header = header;
		this.text = text;
		this.local = local;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public GroupMessageHeader getHeader() {
		return header;
	}

	public String getText() {
		return text;
	}

	public boolean isLocal() {
		return local;
	}

}
