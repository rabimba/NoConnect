package org.chagolchana.chagol.api.data;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.db.Metadata;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MetadataEncoder {

	Metadata encode(BdfDictionary d) throws FormatException;
}
