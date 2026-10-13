package org.chagolchana.chagol.db;

import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.sync.MessageFactory;
import org.chagolchana.chagol.api.system.Clock;
import org.junit.Ignore;

@Ignore
public class H2DatabasePerformanceTest extends SingleDatabasePerformanceTest {

	@Override
	protected String getTestName() {
		return getClass().getSimpleName();
	}

	@Override
	protected JdbcDatabase createDatabase(DatabaseConfig config,
			MessageFactory messageFactory, Clock clock) {
		return new H2Database(config, messageFactory, clock);
	}
}
