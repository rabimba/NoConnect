package org.chagolchana.chagol.api.db;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DbRunnable<E extends Exception> {

	void run(Transaction txn) throws DbException, E;
}
