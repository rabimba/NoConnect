package org.chagolchana.noconnect.api.sharing;

import org.chagolchana.chagol.api.Nameable;
import org.chagolchana.chagol.api.sync.GroupId;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Shareable extends Nameable {

	GroupId getId();

}
