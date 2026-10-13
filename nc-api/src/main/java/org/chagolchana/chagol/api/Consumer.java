package org.chagolchana.chagol.api;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Consumer<T> {

	void accept(T t);
}
