package org.chagolchana.chagol.transport.agreement;

import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s, TransportId transportId);

	BdfDictionary getSessionQuery(TransportId transportId);
}
