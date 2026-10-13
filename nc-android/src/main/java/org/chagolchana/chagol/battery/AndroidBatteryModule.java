package org.chagolchana.chagol.battery;

import org.chagolchana.chagol.api.battery.BatteryManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidBatteryModule {

	public static class EagerSingletons {
		@Inject
		BatteryManager batteryManager;
	}

	@Provides
	@Singleton
	BatteryManager provideBatteryManager(LifecycleManager lifecycleManager,
			AndroidBatteryManager batteryManager) {
		lifecycleManager.registerService(batteryManager);
		return batteryManager;
	}
}
