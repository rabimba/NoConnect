package org.chagolchana.chagol;

import org.chagolchana.chagol.system.TimeTravelModule;

public interface ChagolCoreIntegrationTestEagerSingletons
		extends ChagolCoreEagerSingletons {

	void inject(TimeTravelModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				ChagolCoreIntegrationTestEagerSingletons c) {
			ChagolCoreEagerSingletons.Helper.injectEagerSingletons(c);
			c.inject(new TimeTravelModule.EagerSingletons());
		}
	}
}
