package org.chagolchana.chagol.relay;

import org.chagolchana.chagol.api.UniqueId;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
public class EnvelopeId extends UniqueId {

	public EnvelopeId(byte[] id) {
		super(id);
	}
}
