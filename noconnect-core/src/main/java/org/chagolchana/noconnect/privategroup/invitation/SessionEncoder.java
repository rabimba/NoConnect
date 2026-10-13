package org.chagolchana.noconnect.privategroup.invitation;

import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s);
}
