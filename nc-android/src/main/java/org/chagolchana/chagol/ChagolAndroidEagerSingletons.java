package org.chagolchana.chagol;

import org.chagolchana.chagol.battery.AndroidBatteryModule;
import org.chagolchana.chagol.network.AndroidNetworkModule;
import org.chagolchana.chagol.reporting.ReportingModule;

public interface ChagolAndroidEagerSingletons {

	void inject(AndroidBatteryModule.EagerSingletons init);

	void inject(AndroidNetworkModule.EagerSingletons init);

	void inject(ReportingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				ChagolAndroidEagerSingletons c) {
			c.inject(new AndroidBatteryModule.EagerSingletons());
			c.inject(new AndroidNetworkModule.EagerSingletons());
			c.inject(new ReportingModule.EagerSingletons());
		}
	}
}
