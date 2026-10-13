package org.chagolchana.chagol.db;

import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.api.system.Clock;
import org.junit.Ignore;

import java.sql.Connection;

/**
 * Sanity check for {@link DatabasePerformanceComparisonTest}: check that
 * if conditions A and B are identical, no significant difference is (usually)
 * detected.
 */
@Ignore
public class H2SelfDatabasePerformanceComparisonTest
		extends DatabasePerformanceComparisonTest {

	@Override
	Database<Connection> createDatabase(boolean conditionA,
			DatabaseConfig databaseConfig, MessageFactory messageFactory,
			Clock clock) {
		return new H2Database(databaseConfig, messageFactory, clock);
	}

	@Override
	protected String getTestName() {
		return getClass().getSimpleName();
	}
}
