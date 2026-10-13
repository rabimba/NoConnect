package org.chagolchana.chagol.test;

import org.chagolchana.chagol.battery.DefaultBatteryManagerModule;
import org.chagolchana.chagol.event.DefaultEventExecutorModule;
import org.chagolchana.chagol.system.DefaultWakefulIoExecutorModule;
import org.chagolchana.chagol.system.TimeTravelModule;

import dagger.Module;

@Module(includes = {
		DefaultBatteryManagerModule.class,
		DefaultEventExecutorModule.class,
		DefaultWakefulIoExecutorModule.class,
		TestThreadFactoryModule.class,
		TestDatabaseConfigModule.class,
		TestFeatureFlagModule.class,
		TestMailboxDirectoryModule.class,
		TestSecureRandomModule.class,
		TimeTravelModule.class
})
public class ChagolCoreIntegrationTestModule {

}
