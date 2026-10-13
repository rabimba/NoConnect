package org.chagolchana.noconnect;

import org.chagolchana.noconnect.attachment.AttachmentModule;
import org.chagolchana.noconnect.autodelete.AutoDeleteModule;
import org.chagolchana.noconnect.avatar.AvatarModule;
import org.chagolchana.noconnect.blog.BlogModule;
import org.chagolchana.noconnect.client.NoConnectClientModule;
import org.chagolchana.noconnect.conversation.ConversationModule;
import org.chagolchana.noconnect.feed.FeedModule;
import org.chagolchana.noconnect.forum.ForumModule;
import org.chagolchana.noconnect.identity.IdentityModule;
import org.chagolchana.noconnect.introduction.IntroductionModule;
import org.chagolchana.noconnect.messaging.MessagingModule;
import org.chagolchana.noconnect.privategroup.PrivateGroupModule;
import org.chagolchana.noconnect.privategroup.invitation.GroupInvitationModule;
import org.chagolchana.noconnect.sharing.SharingModule;
import org.chagolchana.noconnect.test.TestModule;

import dagger.Module;

@Module(includes = {
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		NoConnectClientModule.class,
		ConversationModule.class,
		FeedModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		TestModule.class
})
public class NoConnectCoreModule {
}
