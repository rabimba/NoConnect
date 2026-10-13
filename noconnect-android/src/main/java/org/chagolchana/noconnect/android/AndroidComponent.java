package org.chagolchana.noconnect.android;

import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.chagol.ChagolAndroidEagerSingletons;
import org.chagolchana.chagol.ChagolAndroidModule;
import org.chagolchana.chagol.ChagolAppComponent;
import org.chagolchana.chagol.ChagolCoreEagerSingletons;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.account.NoConnectAccountModule;
import org.chagolchana.chagol.api.FeatureFlags;
import org.chagolchana.chagol.api.account.AccountManager;
import org.chagolchana.chagol.api.connection.ConnectionRegistry;
import org.chagolchana.chagol.api.contact.ContactExchangeManager;
import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.crypto.CryptoExecutor;
import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator;
import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.keyagreement.KeyAgreementTask;
import org.chagolchana.chagol.api.keyagreement.PayloadEncoder;
import org.chagolchana.chagol.api.keyagreement.PayloadParser;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.plugin.PluginManager;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.plugin.file.RemovableDriveModule;
import org.chagolchana.chagol.system.ClockModule;
import org.chagolchana.noconnect.NoConnectCoreEagerSingletons;
import org.chagolchana.noconnect.NoConnectCoreModule;
import org.chagolchana.noconnect.android.attachment.AttachmentModule;
import org.chagolchana.noconnect.android.attachment.media.MediaModule;
import org.chagolchana.noconnect.android.contact.connect.BluetoothIntroFragment;
import org.chagolchana.noconnect.android.conversation.glide.NoConnectModelLoader;
import org.chagolchana.noconnect.android.hotspot.AbstractTabsFragment;
import org.chagolchana.noconnect.android.hotspot.FallbackFragment;
import org.chagolchana.noconnect.android.hotspot.HotspotIntroFragment;
import org.chagolchana.noconnect.android.hotspot.ManualHotspotFragment;
import org.chagolchana.noconnect.android.hotspot.QrHotspotFragment;
import org.chagolchana.noconnect.android.logging.CachingLogHandler;
import org.chagolchana.noconnect.android.login.SignInReminderReceiver;
import org.chagolchana.noconnect.android.mailbox.ErrorFragment;
import org.chagolchana.noconnect.android.mailbox.ErrorWizardFragment;
import org.chagolchana.noconnect.android.mailbox.MailboxScanFragment;
import org.chagolchana.noconnect.android.mailbox.MailboxStatusFragment;
import org.chagolchana.noconnect.android.mailbox.OfflineFragment;
import org.chagolchana.noconnect.android.mailbox.SetupDownloadFragment;
import org.chagolchana.noconnect.android.mailbox.SetupIntroFragment;
import org.chagolchana.noconnect.android.removabledrive.ChooserFragment;
import org.chagolchana.noconnect.android.removabledrive.ReceiveFragment;
import org.chagolchana.noconnect.android.removabledrive.SendFragment;
import org.chagolchana.noconnect.android.settings.ConnectionsFragment;
import org.chagolchana.noconnect.android.settings.NotificationsFragment;
import org.chagolchana.noconnect.android.settings.SecurityFragment;
import org.chagolchana.noconnect.android.settings.SettingsFragment;
import org.chagolchana.noconnect.android.view.EmojiTextInputView;
import org.chagolchana.noconnect.api.android.AndroidNotificationManager;
import org.chagolchana.noconnect.api.android.DozeWatchdog;
import org.chagolchana.noconnect.api.android.LockManager;
import org.chagolchana.noconnect.api.android.ScreenFilterMonitor;
import org.chagolchana.noconnect.api.attachment.AttachmentReader;
import org.chagolchana.noconnect.api.autodelete.AutoDeleteManager;
import org.chagolchana.noconnect.api.blog.BlogManager;
import org.chagolchana.noconnect.api.blog.BlogPostFactory;
import org.chagolchana.noconnect.api.blog.BlogSharingManager;
import org.chagolchana.noconnect.api.client.MessageTracker;
import org.chagolchana.noconnect.api.conversation.ConversationManager;
import org.chagolchana.noconnect.api.feed.FeedManager;
import org.chagolchana.noconnect.api.forum.ForumManager;
import org.chagolchana.noconnect.api.forum.ForumSharingManager;
import org.chagolchana.noconnect.api.identity.AuthorManager;
import org.chagolchana.noconnect.api.introduction.IntroductionManager;
import org.chagolchana.noconnect.api.messaging.MessagingManager;
import org.chagolchana.noconnect.api.messaging.PrivateMessageFactory;
import org.chagolchana.noconnect.api.privategroup.GroupMessageFactory;
import org.chagolchana.noconnect.api.privategroup.PrivateGroupFactory;
import org.chagolchana.noconnect.api.privategroup.PrivateGroupManager;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationFactory;
import org.chagolchana.noconnect.api.privategroup.invitation.GroupInvitationManager;
import org.chagolchana.noconnect.api.test.TestDataCreator;
import org.chagolchana.onionwrapper.CircumventionProvider;
import org.chagolchana.onionwrapper.LocationUtils;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import androidx.lifecycle.ViewModelProvider;
import dagger.Component;

@Singleton
@Component(modules = {
		ChagolCoreModule.class,
		NoConnectCoreModule.class,
		ChagolAndroidModule.class,
		NoConnectAccountModule.class,
		AppModule.class,
		AttachmentModule.class,
		ClockModule.class,
		MediaModule.class,
		ModularMailboxModule.class,
		RemovableDriveModule.class
})
public interface AndroidComponent
		extends ChagolCoreEagerSingletons, ChagolAndroidEagerSingletons,
		NoConnectCoreEagerSingletons, AndroidEagerSingletons, ChagolAppComponent {

	// Exposed objects
	@CryptoExecutor
	Executor cryptoExecutor();

	PasswordStrengthEstimator passwordStrengthIndicator();

	@DatabaseExecutor
	Executor databaseExecutor();

	TransactionManager transactionManager();

	MessageTracker messageTracker();

	LifecycleManager lifecycleManager();

	IdentityManager identityManager();

	AttachmentReader attachmentReader();

	AuthorManager authorManager();

	PluginManager pluginManager();

	EventBus eventBus();

	AndroidNotificationManager androidNotificationManager();

	ScreenFilterMonitor screenFilterMonitor();

	ConnectionRegistry connectionRegistry();

	ContactManager contactManager();

	ConversationManager conversationManager();

	MessagingManager messagingManager();

	PrivateMessageFactory privateMessageFactory();

	PrivateGroupManager privateGroupManager();

	GroupInvitationFactory groupInvitationFactory();

	GroupInvitationManager groupInvitationManager();

	PrivateGroupFactory privateGroupFactory();

	GroupMessageFactory groupMessageFactory();

	ForumManager forumManager();

	ForumSharingManager forumSharingManager();

	BlogSharingManager blogSharingManager();

	BlogManager blogManager();

	BlogPostFactory blogPostFactory();

	SettingsManager settingsManager();

	ContactExchangeManager contactExchangeManager();

	KeyAgreementTask keyAgreementTask();

	PayloadEncoder payloadEncoder();

	PayloadParser payloadParser();

	IntroductionManager introductionManager();

	AndroidExecutor androidExecutor();

	FeedManager feedManager();

	Clock clock();

	TestDataCreator testDataCreator();

	DozeWatchdog dozeWatchdog();

	@IoExecutor
	Executor ioExecutor();

	AccountManager accountManager();

	LockManager lockManager();

	LocationUtils locationUtils();

	CircumventionProvider circumventionProvider();

	ViewModelProvider.Factory viewModelFactory();

	FeatureFlags featureFlags();

	AndroidWakeLockManager wakeLockManager();

	CachingLogHandler logHandler();

	Thread.UncaughtExceptionHandler exceptionHandler();

	AutoDeleteManager autoDeleteManager();

	void inject(SignInReminderReceiver noConnectService);

	void inject(NoConnectService noConnectService);

	void inject(NotificationCleanupService notificationCleanupService);

	void inject(EmojiTextInputView textInputView);

	void inject(NoConnectModelLoader noConnectModelLoader);

	void inject(SettingsFragment settingsFragment);

	void inject(ConnectionsFragment connectionsFragment);

	void inject(SecurityFragment securityFragment);

	void inject(NotificationsFragment notificationsFragment);

	void inject(HotspotIntroFragment hotspotIntroFragment);

	void inject(AbstractTabsFragment abstractTabsFragment);

	void inject(QrHotspotFragment qrHotspotFragment);

	void inject(ManualHotspotFragment manualHotspotFragment);

	void inject(FallbackFragment fallbackFragment);

	void inject(ChooserFragment chooserFragment);

	void inject(SendFragment sendFragment);

	void inject(ReceiveFragment receiveFragment);

	void inject(BluetoothIntroFragment bluetoothIntroFragment);

	void inject(SetupIntroFragment setupIntroFragment);

	void inject(SetupDownloadFragment setupDownloadFragment);

	void inject(MailboxScanFragment mailboxScanFragment);

	void inject(OfflineFragment offlineFragment);

	void inject(ErrorFragment errorFragment);

	void inject(MailboxStatusFragment mailboxStatusFragment);

	void inject(ErrorWizardFragment errorWizardFragment);
}
