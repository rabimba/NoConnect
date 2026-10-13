package org.chagolchana.chagol;

import org.chagolchana.chagol.battery.AndroidBatteryModule;
import org.chagolchana.chagol.network.AndroidNetworkModule;

public interface ChagolAndroidEagerSingletons {

	void inject(AndroidBatteryModule.EagerSingletons init);

	void inject(AndroidNetworkModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				ChagolAndroidEagerSingletons c) {
			c.inject(new AndroidBatteryModule.EagerSingletons());
			c.inject(new AndroidNetworkModule.EagerSingletons());
		}
	}
}
