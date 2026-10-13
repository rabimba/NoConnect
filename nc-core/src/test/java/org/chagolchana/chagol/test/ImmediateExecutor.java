package org.chagolchana.chagol.test;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

@NotNullByDefault
public class ImmediateExecutor implements Executor {

	@Override
	public void execute(Runnable r) {
		r.run();
	}
}
