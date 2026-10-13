package org.chagolchana.chagol.api;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Predicate<T> {

	boolean test(T t);
}
