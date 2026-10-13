package org.chagolchana.chagol.api.keyagreement;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PayloadEncoder {

	byte[] encode(Payload p);
}
