package org.chagolchana.noconnect.messaging;

import org.chagolchana.chagol.ChagolCoreIntegrationTestEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.test.ChagolCoreIntegrationTestModule;
import org.chagolchana.chagol.test.TestDnsModule;
import org.chagolchana.chagol.test.TestPluginConfigModule;
import org.chagolchana.chagol.test.TestSocksModule;
import org.chagolchana.noconnect.autodelete.AutoDeleteModule;
import org.chagolchana.noconnect.avatar.AvatarModule;
import org.chagolchana.noconnect.client.NoConnectClientModule;
import org.chagolchana.noconnect.conversation.ConversationModule;
import org.chagolchana.noconnect.forum.ForumModule;
import org.chagolchana.noconnect.identity.IdentityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		ChagolCoreIntegrationTestModule.class,
		ChagolCoreModule.class,
		NoConnectClientModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		ConversationModule.class,
		ForumModule.class,
		IdentityModule.class,
		MessagingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface MessageSizeIntegrationTestComponent
		extends ChagolCoreIntegrationTestEagerSingletons {

	void inject(MessageSizeIntegrationTest testCase);

	void inject(AvatarModule.EagerSingletons init);

	void inject(ForumModule.EagerSingletons init);

	void inject(MessagingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				MessageSizeIntegrationTestComponent c) {
			ChagolCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
		}
	}
}
