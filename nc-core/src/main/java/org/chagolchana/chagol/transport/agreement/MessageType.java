package org.chagolchana.chagol.transport.agreement;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
enum MessageType {

	KEY(0),
	ACTIVATE(1);

	private final int value;

	MessageType(int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}

	static MessageType fromValue(int value) throws FormatException {
		for (MessageType t : values()) if (t.value == value) return t;
		throw new FormatException();
	}
}
