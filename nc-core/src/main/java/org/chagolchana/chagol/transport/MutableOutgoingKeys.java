package org.chagolchana.chagol.transport;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.transport.OutgoingKeys;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class MutableOutgoingKeys {

	private final SecretKey tagKey, headerKey;
	private final long timePeriod;
	private long streamCounter;
	private boolean active;

	MutableOutgoingKeys(OutgoingKeys out) {
		tagKey = out.getTagKey();
		headerKey = out.getHeaderKey();
		timePeriod = out.getTimePeriod();
		streamCounter = out.getStreamCounter();
		active = out.isActive();
	}

	OutgoingKeys snapshot() {
		return new OutgoingKeys(tagKey, headerKey, timePeriod,
				streamCounter, active);
	}

	SecretKey getTagKey() {
		return tagKey;
	}

	SecretKey getHeaderKey() {
		return headerKey;
	}

	long getTimePeriod() {
		return timePeriod;
	}

	long getStreamCounter() {
		return streamCounter;
	}

	void incrementStreamCounter() {
		streamCounter++;
	}

	boolean isActive() {
		return active;
	}

	void activate() {
		active = true;
	}
}
