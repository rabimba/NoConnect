package org.chagolchana.chagol.api.keyagreement;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface PayloadParser {

	Payload parse(String payload) throws IOException;
}
