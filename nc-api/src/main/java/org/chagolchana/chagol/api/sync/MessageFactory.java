package org.chagolchana.chagol.api.sync;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MessageFactory {

	Message createMessage(GroupId g, long timestamp, byte[] body);

	Message createMessage(byte[] raw);

	byte[] getRawMessage(Message m);
}
