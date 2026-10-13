package org.chagolchana.chagol.battery;

import org.chagolchana.chagol.api.battery.BatteryManager;

import dagger.Module;
import dagger.Provides;

/**
 * Provides a default implementation of {@link BatteryManager} for systems
 * without batteries.
 */
@Module
public class DefaultBatteryManagerModule {

	@Provides
	BatteryManager provideBatteryManager() {
		return () -> false;
	}
}
