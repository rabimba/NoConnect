package org.chagolchana.noconnect.introduction;

import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.test.ChagolCoreIntegrationTestModule;
import org.chagolchana.chagol.test.TestDnsModule;
import org.chagolchana.chagol.test.TestPluginConfigModule;
import org.chagolchana.chagol.test.TestSocksModule;
import org.chagolchana.noconnect.attachment.AttachmentModule;
import org.chagolchana.noconnect.autodelete.AutoDeleteModule;
import org.chagolchana.noconnect.avatar.AvatarModule;
import org.chagolchana.noconnect.blog.BlogModule;
import org.chagolchana.noconnect.client.NoConnectClientModule;
import org.chagolchana.noconnect.conversation.ConversationModule;
import org.chagolchana.noconnect.forum.ForumModule;
import org.chagolchana.noconnect.identity.IdentityModule;
import org.chagolchana.noconnect.messaging.MessagingModule;
import org.chagolchana.noconnect.privategroup.PrivateGroupModule;
import org.chagolchana.noconnect.privategroup.invitation.GroupInvitationModule;
import org.chagolchana.noconnect.sharing.SharingModule;
import org.chagolchana.noconnect.test.NoConnectIntegrationTestComponent;

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
interface IntroductionIntegrationTestComponent
		extends NoConnectIntegrationTestComponent {

	void inject(IntroductionIntegrationTest init);

	void inject(MessageEncoderParserIntegrationTest init);

	void inject(SessionEncoderParserIntegrationTest init);

	void inject(IntroductionCryptoIntegrationTest init);

	void inject(AutoDeleteIntegrationTest init);

	MessageEncoder getMessageEncoder();

	MessageParser getMessageParser();

	SessionParser getSessionParser();

	IntroductionCrypto getIntroductionCrypto();

}
