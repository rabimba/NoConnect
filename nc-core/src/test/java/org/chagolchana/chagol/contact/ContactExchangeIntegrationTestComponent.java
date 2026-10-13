package org.chagolchana.chagol.contact;

import org.chagolchana.chagol.ChagolCoreIntegrationTestEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.api.connection.ConnectionManager;
import org.chagolchana.chagol.api.contact.ContactExchangeManager;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.test.ChagolCoreIntegrationTestModule;
import org.chagolchana.chagol.test.TestDnsModule;
import org.chagolchana.chagol.test.TestPluginConfigModule;
import org.chagolchana.chagol.test.TestSocksModule;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		ChagolCoreIntegrationTestModule.class,
		ChagolCoreModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface ContactExchangeIntegrationTestComponent
		extends ChagolCoreIntegrationTestEagerSingletons {

	ConnectionManager getConnectionManager();

	ContactExchangeManager getContactExchangeManager();

	ContactManager getContactManager();

	EventBus getEventBus();

	IdentityManager getIdentityManager();

	@IoExecutor
	Executor getIoExecutor();

	LifecycleManager getLifecycleManager();
}
