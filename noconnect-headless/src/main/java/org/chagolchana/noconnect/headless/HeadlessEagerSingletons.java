package org.chagolchana.noconnect.headless;

import org.chagolchana.chagol.system.DefaultTaskSchedulerModule;

public interface HeadlessEagerSingletons {

	void inject(DefaultTaskSchedulerModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(HeadlessEagerSingletons c) {
			c.inject(new DefaultTaskSchedulerModule.EagerSingletons());
		}
	}
}
