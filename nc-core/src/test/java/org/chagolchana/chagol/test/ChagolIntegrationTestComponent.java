package org.chagolchana.chagol.test;

import org.chagolchana.chagol.ChagolCoreIntegrationTestEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.api.client.ClientHelper;
import org.chagolchana.chagol.api.connection.ConnectionManager;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;

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
public interface ChagolIntegrationTestComponent
		extends ChagolCoreIntegrationTestEagerSingletons {

	IdentityManager getIdentityManager();

	EventBus getEventBus();

	ConnectionManager getConnectionManager();

	ClientHelper getClientHelper();

}
