package org.chagolchana.noconnect.test;

import org.chagolchana.chagol.ChagolCoreIntegrationTestEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.db.DatabaseComponent;
import org.chagolchana.chagol.api.identity.AuthorFactory;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.properties.TransportPropertyManager;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.test.ChagolCoreIntegrationTestModule;
import org.chagolchana.chagol.test.ChagolIntegrationTestComponent;
import org.chagolchana.chagol.test.TestDnsModule;
import org.chagolchana.chagol.test.TestPluginConfigModule;
import org.chagolchana.chagol.test.TestSocksModule;
import org.chagolchana.chagol.test.TimeTravel;
import org.chagolchana.noconnect.api.attachment.AttachmentReader;
import org.chagolchana.noconnect.api.autodelete.AutoDeleteManager;
import org.chagolchana.noconnect.api.avatar.AvatarManager;
import org.chagolchana.noconnect.api.blog.BlogFactory;
import org.chagolchana.noconnect.api.blog.BlogManager;
import org.chagolchana.noconnect.api.blog.BlogSharingManager;
import org.chagolchana.noconnect.api.client.MessageTracker;
import org.chagolchana.noconnect.api.conversation.ConversationManager;
import org.chagolchana.noconnect.api.forum.ForumManager;
import org.chagolchana.noconnect.api.forum.ForumSharingManager;
import org.chagolchana.noconnect.api.introduction.IntroductionManager;
import org.chagolchana.noconnect.api.messaging.MessagingManager;
import org.chagolchana.noconnect.api.messaging.PrivateMessageFactory;
import org.chagolchana.noconnect.api.privategroup.PrivateGroupManager;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationFactory;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationManager;
import org.chagolchana.noconnect.attachment.AttachmentModule;
import org.chagolchana.noconnect.autodelete.AutoDeleteModule;
import org.chagolchana.noconnect.avatar.AvatarModule;
import org.chagolchana.noconnect.blog.BlogModule;
import org.chagolchana.noconnect.client.NoConnectClientModule;
import org.chagolchana.noconnect.conversation.ConversationModule;
import org.chagolchana.noconnect.forum.ForumModule;
import org.chagolchana.noconnect.identity.IdentityModule;
import org.chagolchana.noconnect.introduction.IntroductionModule;
import org.chagolchana.noconnect.messaging.MessagingModule;
import org.chagolchana.noconnect.privategroup.PrivateGroupModule;
import org.chagolchana.noconnect.privategroup.invitation.GroupInvitationModule;
import org.chagolchana.noconnect.sharing.SharingModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		ChagolCoreIntegrationTestModule.class,
		ChagolCoreModule.class,
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		NoConnectClientModule.class,
		ConversationModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
public interface NoConnectIntegrationTestComponent
		extends ChagolIntegrationTestComponent {

	void inject(NoConnectIntegrationTest<NoConnectIntegrationTestComponent> init);

	void inject(AutoDeleteModule.EagerSingletons init);

	void inject(AvatarModule.EagerSingletons init);

	void inject(BlogModule.EagerSingletons init);

	void inject(ConversationModule.EagerSingletons init);

	void inject(ForumModule.EagerSingletons init);

	void inject(GroupInvitationModule.EagerSingletons init);

	void inject(IdentityModule.EagerSingletons init);

	void inject(IntroductionModule.EagerSingletons init);

	void inject(MessagingModule.EagerSingletons init);

	void inject(PrivateGroupModule.EagerSingletons init);

	void inject(SharingModule.EagerSingletons init);

	LifecycleManager getLifecycleManager();

	AttachmentReader getAttachmentReader();

	AvatarManager getAvatarManager();

	ContactManager getContactManager();

	ConversationManager getConversationManager();

	DatabaseComponent getDatabaseComponent();

	BlogManager getBlogManager();

	BlogSharingManager getBlogSharingManager();

	ForumSharingManager getForumSharingManager();

	ForumManager getForumManager();

	GroupInvitationManager getGroupInvitationManager();

	GroupInvitationFactory getGroupInvitationFactory();

	IntroductionManager getIntroductionManager();

	MessageTracker getMessageTracker();

	MessagingManager getMessagingManager();

	PrivateGroupManager getPrivateGroupManager();

	PrivateMessageFactory getPrivateMessageFactory();

	TransportPropertyManager getTransportPropertyManager();

	AuthorFactory getAuthorFactory();

	BlogFactory getBlogFactory();

	AutoDeleteManager getAutoDeleteManager();

	Clock getClock();

	TimeTravel getTimeTravel();

	class Helper {

		public static void injectEagerSingletons(
				NoConnectIntegrationTestComponent c) {
			ChagolCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AutoDeleteModule.EagerSingletons());
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new BlogModule.EagerSingletons());
			c.inject(new ConversationModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new GroupInvitationModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new IntroductionModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
			c.inject(new PrivateGroupModule.EagerSingletons());
			c.inject(new SharingModule.EagerSingletons());
		}
	}
}
