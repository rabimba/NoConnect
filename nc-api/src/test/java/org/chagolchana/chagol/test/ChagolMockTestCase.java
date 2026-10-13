package org.chagolchana.chagol.test;

import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;

public abstract class ChagolMockTestCase extends ChagolTestCase {

	protected final Mockery context = new Mockery();

	public ChagolMockTestCase() {
		context.setThreadingPolicy(new Synchroniser());
	}

	@After
	public void checkExpectations() {
		context.assertIsSatisfied();
	}
}
