package org.chagolchana.chagol;

import org.chagolchana.chagol.network.JavaNetworkModule;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ChagolJavaEagerSingletons {

	void inject(JavaNetworkModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(ChagolJavaEagerSingletons c) {
			c.inject(new JavaNetworkModule.EagerSingletons());
		}
	}
}
