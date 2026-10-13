package org.chagolchana.noconnect.introduction;

import org.chagolchana.chagol.api.data.BdfDictionary;
import org.chagolchana.chagol.api.identity.Author;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary getIntroduceeSessionsByIntroducerQuery(Author introducer);

	BdfDictionary getIntroducerSessionsQuery();

	BdfDictionary encodeIntroducerSession(IntroducerSession s);

	BdfDictionary encodeIntroduceeSession(IntroduceeSession s);

}
