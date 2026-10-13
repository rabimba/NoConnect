package org.chagolchana.chagol.db;

import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.api.system.Clock;
import org.junit.Ignore;

import java.sql.Connection;

@Ignore
public class H2HyperSqlDatabasePerformanceComparisonTest
		extends DatabasePerformanceComparisonTest {

	@Override
	Database<Connection> createDatabase(boolean conditionA,
			DatabaseConfig databaseConfig, MessageFactory messageFactory,
			Clock clock) {
		if (conditionA)
			return new H2Database(databaseConfig, messageFactory, clock);
		else return new HyperSqlDatabase(databaseConfig, messageFactory, clock);
	}

	@Override
	protected String getTestName() {
		return getClass().getSimpleName();
	}
}
