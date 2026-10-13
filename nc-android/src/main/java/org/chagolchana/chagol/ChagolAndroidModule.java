package org.chagolchana.chagol;

import org.chagolchana.chagol.battery.AndroidBatteryModule;
import org.chagolchana.chagol.io.DnsModule;
import org.chagolchana.chagol.network.AndroidNetworkModule;
import org.chagolchana.chagol.plugin.tor.CircumventionModule;
import org.chagolchana.chagol.reporting.ReportingModule;
import org.chagolchana.chagol.socks.SocksModule;
import org.chagolchana.chagol.system.AndroidSystemModule;
import org.chagolchana.chagol.system.AndroidTaskSchedulerModule;
import org.chagolchana.chagol.system.AndroidWakeLockModule;
import org.chagolchana.chagol.system.AndroidWakefulIoExecutorModule;
import org.chagolchana.chagol.system.DefaultThreadFactoryModule;

import dagger.Module;

@Module(includes = {
		AndroidBatteryModule.class,
		AndroidNetworkModule.class,
		AndroidSystemModule.class,
		AndroidTaskSchedulerModule.class,
		AndroidWakefulIoExecutorModule.class,
		AndroidWakeLockModule.class,
		DefaultThreadFactoryModule.class,
		CircumventionModule.class,
		DnsModule.class,
		ReportingModule.class,
		SocksModule.class
})
public class ChagolAndroidModule {
}
