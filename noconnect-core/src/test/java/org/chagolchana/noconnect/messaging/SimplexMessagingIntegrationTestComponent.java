package org.chagolchana.noconnect.messaging;

import org.chagolchana.chagol.ChagolCoreIntegrationTestEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.api.connection.ConnectionManager;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.test.ChagolCoreIntegrationTestModule;
import org.chagolchana.chagol.test.TestDnsModule;
import org.chagolchana.chagol.test.TestPluginConfigModule;
import org.chagolchana.chagol.test.TestSocksModule;
import org.chagolchana.noconnect.api.messaging.MessagingManager;
import org.chagolchana.noconnect.api.messaging.PrivateMessageFactory;
import org.chagolchana.noconnect.autodelete.AutoDeleteModule;
import org.chagolchana.noconnect.client.NoConnectClientModule;
import org.chagolchana.noconnect.conversation.ConversationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		AutoDeleteModule.class,
		ChagolCoreIntegrationTestModule.class,
		ChagolCoreModule.class,
		NoConnectClientModule.class,
		ConversationModule.class,
		MessagingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface SimplexMessagingIntegrationTestComponent
		extends ChagolCoreIntegrationTestEagerSingletons {

	void inject(MessagingModule.EagerSingletons init);

	LifecycleManager getLifecycleManager();

	IdentityManager getIdentityManager();

	ContactManager getContactManager();

	MessagingManager getMessagingManager();

	PrivateMessageFactory getPrivateMessageFactory();

	EventBus getEventBus();

	ConnectionManager getConnectionManager();

	class Helper {

		public static void injectEagerSingletons(
				SimplexMessagingIntegrationTestComponent c) {
			ChagolCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new MessagingModule.EagerSingletons());
		}
	}
}
