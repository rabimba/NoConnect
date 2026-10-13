package org.chagolchana.chagol.plugin.file;

import org.chagolchana.chagol.ChagolCoreEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.plugin.file.RemovableDriveManager;
import org.chagolchana.chagol.battery.DefaultBatteryManagerModule;
import org.chagolchana.chagol.event.DefaultEventExecutorModule;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.system.DefaultThreadFactoryModule;
import org.chagolchana.chagol.system.DefaultWakefulIoExecutorModule;
import org.chagolchana.chagol.system.TimeTravelModule;
import org.chagolchana.chagol.test.TestDatabaseConfigModule;
import org.chagolchana.chagol.test.TestDnsModule;
import org.chagolchana.chagol.test.TestFeatureFlagModule;
import org.chagolchana.chagol.test.TestMailboxDirectoryModule;
import org.chagolchana.chagol.test.TestSecureRandomModule;
import org.chagolchana.chagol.test.TestSocksModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		ChagolCoreModule.class,
		DefaultBatteryManagerModule.class,
		DefaultEventExecutorModule.class,
		DefaultWakefulIoExecutorModule.class,
		DefaultThreadFactoryModule.class,
		TestDatabaseConfigModule.class,
		TestDnsModule.class,
		TestFeatureFlagModule.class,
		TestMailboxDirectoryModule.class,
		RemovableDriveIntegrationTestModule.class,
		RemovableDriveModule.class,
		ModularMailboxModule.class,
		TestSecureRandomModule.class,
		TimeTravelModule.class,
		TestSocksModule.class,
})
interface RemovableDriveIntegrationTestComponent
		extends ChagolCoreEagerSingletons {

	ContactManager getContactManager();

	EventBus getEventBus();

	IdentityManager getIdentityManager();

	LifecycleManager getLifecycleManager();

	RemovableDriveManager getRemovableDriveManager();

	class Helper {

		public static void injectEagerSingletons(
				RemovableDriveIntegrationTestComponent c) {
			ChagolCoreEagerSingletons.Helper.injectEagerSingletons(c);
		}
	}
}
