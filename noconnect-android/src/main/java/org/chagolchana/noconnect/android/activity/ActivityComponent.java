package org.chagolchana.noconnect.android.activity;

import android.app.Activity;

import org.chagolchana.noconnect.android.AndroidComponent;
import org.chagolchana.noconnect.android.StartupFailureActivity;
import org.chagolchana.noconnect.android.account.SetupActivity;
import org.chagolchana.noconnect.android.account.SetupFragment;
import org.chagolchana.noconnect.android.account.UnlockActivity;
import org.chagolchana.noconnect.android.blog.BlogActivity;
import org.chagolchana.noconnect.android.blog.BlogFragment;
import org.chagolchana.noconnect.android.blog.BlogPostFragment;
import org.chagolchana.noconnect.android.blog.FeedFragment;
import org.chagolchana.noconnect.android.blog.ReblogActivity;
import org.chagolchana.noconnect.android.blog.ReblogFragment;
import org.chagolchana.noconnect.android.blog.WriteBlogPostActivity;
import org.chagolchana.noconnect.android.contact.ContactListFragment;
import org.chagolchana.noconnect.android.contact.add.nearby.AddNearbyContactActivity;
import org.chagolchana.noconnect.android.contact.add.nearby.AddNearbyContactErrorFragment;
import org.chagolchana.noconnect.android.contact.add.nearby.AddNearbyContactFragment;
import org.chagolchana.noconnect.android.contact.add.nearby.AddNearbyContactIntroFragment;
import org.chagolchana.noconnect.android.contact.add.remote.AddContactActivity;
import org.chagolchana.noconnect.android.contact.add.remote.LinkExchangeFragment;
import org.chagolchana.noconnect.android.contact.add.remote.NicknameFragment;
import org.chagolchana.noconnect.android.contact.add.remote.PendingContactListActivity;
import org.chagolchana.noconnect.android.contact.connect.ConnectViaBluetoothActivity;
import org.chagolchana.noconnect.android.conversation.AliasDialogFragment;
import org.chagolchana.noconnect.android.conversation.ConversationActivity;
import org.chagolchana.noconnect.android.conversation.ConversationSettingsDialog;
import org.chagolchana.noconnect.android.conversation.ImageActivity;
import org.chagolchana.noconnect.android.conversation.ImageFragment;
import org.chagolchana.noconnect.android.forum.CreateForumActivity;
import org.chagolchana.noconnect.android.forum.ForumActivity;
import org.chagolchana.noconnect.android.forum.ForumListFragment;
import org.chagolchana.noconnect.android.fragment.ScreenFilterDialogFragment;
import org.chagolchana.noconnect.android.hotspot.HotspotActivity;
import org.chagolchana.noconnect.android.update.InstallUpdateActivity;
import org.chagolchana.noconnect.android.introduction.ContactChooserFragment;
import org.chagolchana.noconnect.android.introduction.IntroductionActivity;
import org.chagolchana.noconnect.android.introduction.IntroductionMessageFragment;
import org.chagolchana.noconnect.android.login.ChangePasswordActivity;
import org.chagolchana.noconnect.android.login.OpenDatabaseFragment;
import org.chagolchana.noconnect.android.login.PasswordFragment;
import org.chagolchana.noconnect.android.login.StartupActivity;
import org.chagolchana.noconnect.android.mailbox.MailboxActivity;
import org.chagolchana.noconnect.android.navdrawer.NavDrawerActivity;
import org.chagolchana.noconnect.android.navdrawer.TransportsActivity;
import org.chagolchana.noconnect.android.panic.PanicPreferencesActivity;
import org.chagolchana.noconnect.android.panic.PanicResponderActivity;
import org.chagolchana.noconnect.android.privategroup.conversation.GroupActivity;
import org.chagolchana.noconnect.android.privategroup.creation.CreateGroupActivity;
import org.chagolchana.noconnect.android.privategroup.creation.CreateGroupFragment;
import org.chagolchana.noconnect.android.privategroup.creation.CreateGroupModule;
import org.chagolchana.noconnect.android.privategroup.creation.GroupInviteActivity;
import org.chagolchana.noconnect.android.privategroup.creation.GroupInviteFragment;
import org.chagolchana.noconnect.android.privategroup.invitation.GroupInvitationActivity;
import org.chagolchana.noconnect.android.privategroup.invitation.GroupInvitationModule;
import org.chagolchana.noconnect.android.privategroup.list.GroupListFragment;
import org.chagolchana.noconnect.android.privategroup.memberlist.GroupMemberListActivity;
import org.chagolchana.noconnect.android.privategroup.memberlist.GroupMemberModule;
import org.chagolchana.noconnect.android.privategroup.reveal.GroupRevealModule;
import org.chagolchana.noconnect.android.privategroup.reveal.RevealContactsActivity;
import org.chagolchana.noconnect.android.privategroup.reveal.RevealContactsFragment;
import org.chagolchana.noconnect.android.removabledrive.RemovableDriveActivity;
import org.chagolchana.noconnect.android.reporting.CrashFragment;
import org.chagolchana.noconnect.android.reporting.CrashReportActivity;
import org.chagolchana.noconnect.android.reporting.ReportFormFragment;
import org.chagolchana.noconnect.android.settings.ConfirmAvatarDialogFragment;
import org.chagolchana.noconnect.android.settings.SettingsActivity;
import org.chagolchana.noconnect.android.settings.SettingsFragment;
import org.chagolchana.noconnect.android.sharing.BlogInvitationActivity;
import org.chagolchana.noconnect.android.sharing.BlogSharingStatusActivity;
import org.chagolchana.noconnect.android.sharing.ForumInvitationActivity;
import org.chagolchana.noconnect.android.sharing.ForumSharingStatusActivity;
import org.chagolchana.noconnect.android.sharing.ShareBlogActivity;
import org.chagolchana.noconnect.android.sharing.ShareBlogFragment;
import org.chagolchana.noconnect.android.sharing.ShareForumActivity;
import org.chagolchana.noconnect.android.sharing.ShareForumFragment;
import org.chagolchana.noconnect.android.sharing.SharingModule;
import org.chagolchana.noconnect.android.splash.SplashScreenActivity;
import org.chagolchana.noconnect.android.test.TestDataActivity;

import dagger.Component;

@ActivityScope
@Component(modules = {
		ActivityModule.class,
		CreateGroupModule.class,
		GroupInvitationModule.class,
		GroupMemberModule.class,
		GroupRevealModule.class,
		SharingModule.SharingLegacyModule.class
}, dependencies = AndroidComponent.class)
public interface ActivityComponent {

	Activity activity();

	void inject(SplashScreenActivity activity);

	void inject(StartupActivity activity);

	void inject(SetupActivity activity);

	void inject(NavDrawerActivity activity);

	void inject(PanicResponderActivity activity);

	void inject(PanicPreferencesActivity activity);

	void inject(AddNearbyContactActivity activity);

	void inject(ConversationActivity activity);

	void inject(ImageActivity activity);

	void inject(ForumInvitationActivity activity);

	void inject(BlogInvitationActivity activity);

	void inject(CreateGroupActivity activity);

	void inject(GroupActivity activity);

	void inject(GroupInviteActivity activity);

	void inject(GroupInvitationActivity activity);

	void inject(GroupMemberListActivity activity);

	void inject(RevealContactsActivity activity);

	void inject(CreateForumActivity activity);

	void inject(ShareForumActivity activity);

	void inject(ShareBlogActivity activity);

	void inject(ForumSharingStatusActivity activity);

	void inject(BlogSharingStatusActivity activity);

	void inject(ForumActivity activity);

	void inject(BlogActivity activity);

	void inject(WriteBlogPostActivity activity);

	void inject(BlogFragment fragment);

	void inject(BlogPostFragment fragment);

	void inject(ReblogFragment fragment);

	void inject(ReblogActivity activity);

	void inject(SettingsActivity activity);

	void inject(TransportsActivity activity);

	void inject(TestDataActivity activity);

	void inject(ChangePasswordActivity activity);

	void inject(IntroductionActivity activity);

	void inject(StartupFailureActivity activity);

	void inject(UnlockActivity activity);

	void inject(AddContactActivity activity);

	void inject(PendingContactListActivity activity);

	void inject(CrashReportActivity crashReportActivity);

	void inject(HotspotActivity hotspotActivity);

	void inject(InstallUpdateActivity installUpdateActivity);

	void inject(RemovableDriveActivity activity);

	// Fragments

	void inject(SetupFragment fragment);

	void inject(PasswordFragment imageFragment);

	void inject(OpenDatabaseFragment activity);

	void inject(ContactListFragment fragment);

	void inject(CreateGroupFragment fragment);

	void inject(GroupListFragment fragment);

	void inject(GroupInviteFragment fragment);

	void inject(RevealContactsFragment activity);

	void inject(ForumListFragment fragment);

	void inject(FeedFragment fragment);

	void inject(AddNearbyContactIntroFragment fragment);

	void inject(AddNearbyContactFragment fragment);

	void inject(LinkExchangeFragment fragment);

	void inject(NicknameFragment fragment);

	void inject(ContactChooserFragment fragment);

	void inject(ShareForumFragment fragment);

	void inject(ShareBlogFragment fragment);

	void inject(IntroductionMessageFragment fragment);

	void inject(SettingsFragment fragment);

	void inject(ScreenFilterDialogFragment fragment);

	void inject(AddNearbyContactErrorFragment fragment);

	void inject(AliasDialogFragment aliasDialogFragment);

	void inject(ImageFragment imageFragment);

	void inject(ReportFormFragment reportFormFragment);

	void inject(CrashFragment crashFragment);

	void inject(ConfirmAvatarDialogFragment fragment);

	void inject(ConversationSettingsDialog dialog);

	void inject(ConnectViaBluetoothActivity connectViaBluetoothActivity);

	void inject(MailboxActivity mailboxActivity);
}
