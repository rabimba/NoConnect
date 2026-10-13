package org.chagolchana.noconnect.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.vanniktech.emoji.RecentEmoji;

import org.chagolchana.chagol.api.FeatureFlags;
import org.chagolchana.chagol.api.crypto.KeyStrengthener;
import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.mailbox.MailboxDirectory;
import org.chagolchana.chagol.api.plugin.BleConstants;
import org.chagolchana.chagol.api.plugin.BluetoothConstants;
import org.chagolchana.chagol.api.plugin.LanTcpConstants;
import org.chagolchana.chagol.api.plugin.PluginConfig;
import org.chagolchana.chagol.api.plugin.TorControlPort;
import org.chagolchana.chagol.api.plugin.TorDirectory;
import org.chagolchana.chagol.api.plugin.TorSocksPort;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPluginFactory;
import org.chagolchana.chagol.api.plugin.simplex.SimplexPluginFactory;
import org.chagolchana.chagol.api.reporting.DevConfig;
import org.chagolchana.chagol.plugin.ble.AndroidBlePluginFactory;
import org.chagolchana.chagol.plugin.bluetooth.AndroidBluetoothPluginFactory;
import org.chagolchana.chagol.plugin.file.AndroidRemovableDrivePluginFactory;
import org.chagolchana.chagol.plugin.file.MailboxPluginFactory;
import org.chagolchana.chagol.plugin.tcp.AndroidLanTcpPluginFactory;
import org.chagolchana.chagol.plugin.tor.AndroidTorPluginFactory;
import org.chagolchana.chagol.util.AndroidUtils;
import org.chagolchana.noconnect.android.account.DozeHelperModule;
import org.chagolchana.noconnect.android.account.LockManagerImpl;
import org.chagolchana.noconnect.android.account.SetupModule;
import org.chagolchana.noconnect.android.blog.BlogModule;
import org.chagolchana.noconnect.android.contact.ContactListModule;
import org.chagolchana.noconnect.android.contact.add.nearby.AddNearbyContactModule;
import org.chagolchana.noconnect.android.contact.connect.ConnectViaBluetoothModule;
import org.chagolchana.noconnect.android.forum.ForumModule;
import org.chagolchana.noconnect.android.hotspot.HotspotModule;
import org.chagolchana.noconnect.android.introduction.IntroductionModule;
import org.chagolchana.noconnect.android.logging.LoggingModule;
import org.chagolchana.noconnect.android.login.LoginModule;
import org.chagolchana.noconnect.android.mailbox.MailboxModule;
import org.chagolchana.noconnect.android.navdrawer.NavDrawerModule;
import org.chagolchana.noconnect.android.privategroup.conversation.GroupConversationModule;
import org.chagolchana.noconnect.android.privategroup.list.GroupListModule;
import org.chagolchana.noconnect.android.removabledrive.TransferDataModule;
import org.chagolchana.noconnect.android.reporting.DevReportModule;
import org.chagolchana.noconnect.android.settings.SettingsModule;
import org.chagolchana.noconnect.android.sharing.SharingModule;
import org.chagolchana.noconnect.android.test.TestAvatarCreatorImpl;
import org.chagolchana.noconnect.android.viewmodel.ViewModelModule;
import org.chagolchana.noconnect.api.android.AndroidNotificationManager;
import org.chagolchana.noconnect.api.android.DozeWatchdog;
import org.chagolchana.noconnect.api.android.LockManager;
import org.chagolchana.noconnect.api.android.NetworkUsageMetrics;
import org.chagolchana.noconnect.api.android.ScreenFilterMonitor;
import org.chagolchana.noconnect.api.test.TestAvatarCreator;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Build.VERSION.SDK_INT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.chagolchana.chagol.api.plugin.TorConstants.DEFAULT_CONTROL_PORT;
import static org.chagolchana.chagol.api.plugin.TorConstants.DEFAULT_SOCKS_PORT;
import static org.chagolchana.noconnect.android.TestingConstants.IS_DEBUG_BUILD;

@Module(includes = {
		SetupModule.class,
		DozeHelperModule.class,
		AddNearbyContactModule.class,
		LoggingModule.class,
		LoginModule.class,
		NavDrawerModule.class,
		ViewModelModule.class,
		SettingsModule.class,
		DevReportModule.class,
		ContactListModule.class,
		IntroductionModule.class,
		ConnectViaBluetoothModule.class,
		// below need to be within same scope as ViewModelProvider.Factory
		BlogModule.class,
		ForumModule.class,
		GroupListModule.class,
		GroupConversationModule.class,
		SharingModule.class,
		HotspotModule.class,
		TransferDataModule.class,
		MailboxModule.class,
})
public class AppModule {

	static class EagerSingletons {
		@Inject
		AndroidNotificationManager androidNotificationManager;
		@Inject
		ScreenFilterMonitor screenFilterMonitor;
		@Inject
		NetworkUsageMetrics networkUsageMetrics;
		@Inject
		DozeWatchdog dozeWatchdog;
		@Inject
		LockManager lockManager;
		@Inject
		RecentEmoji recentEmoji;
	}

	private final Application application;

	public AppModule(Application application) {
		this.application = application;
	}

	public static AndroidComponent getAndroidComponent(Context ctx) {
		NoConnectApplication app = (NoConnectApplication) ctx.getApplicationContext();
		return app.getApplicationComponent();
	}

	@Provides
	@Singleton
	Application providesApplication() {
		return application;
	}

	@Provides
	@Singleton
	DatabaseConfig provideDatabaseConfig(Application app) {
		//FIXME: StrictMode
		StrictMode.ThreadPolicy tp = StrictMode.allowThreadDiskReads();
		StrictMode.allowThreadDiskWrites();
		File dbDir = app.getApplicationContext().getDir("db", MODE_PRIVATE);
		File keyDir = app.getApplicationContext().getDir("key", MODE_PRIVATE);
		StrictMode.setThreadPolicy(tp);
		KeyStrengthener keyStrengthener = SDK_INT >= 23
				? new AndroidKeyStrengthener() : null;
		return new AndroidDatabaseConfig(dbDir, keyDir, keyStrengthener);
	}

	@Provides
	@Singleton
	@MailboxDirectory
	File provideMailboxDirectory(Application app) {
		return app.getDir("mailbox", MODE_PRIVATE);
	}

	@Provides
	@Singleton
	@TorDirectory
	File provideTorDirectory(Application app) {
		return app.getDir("tor", MODE_PRIVATE);
	}

	@Provides
	@Singleton
	@TorSocksPort
	int provideTorSocksPort() {
		if (!IS_DEBUG_BUILD) {
			return DEFAULT_SOCKS_PORT;
		} else {
			return DEFAULT_SOCKS_PORT + 2;
		}
	}

	@Provides
	@Singleton
	@TorControlPort
	int provideTorControlPort() {
		if (!IS_DEBUG_BUILD) {
			return DEFAULT_CONTROL_PORT;
		} else {
			return DEFAULT_CONTROL_PORT + 2;
		}
	}

	@Provides
	@Singleton
	PluginConfig providePluginConfig(AndroidBluetoothPluginFactory bluetooth,
			AndroidBlePluginFactory ble,
			AndroidTorPluginFactory tor, AndroidLanTcpPluginFactory lan,
			AndroidRemovableDrivePluginFactory drive,
			MailboxPluginFactory mailbox, FeatureFlags featureFlags) {
		@NotNullByDefault
		PluginConfig pluginConfig = new PluginConfig() {

			@Override
			public Collection<DuplexPluginFactory> getDuplexFactories() {
				if (featureFlags.shouldEnableBleTransport()) {
					return asList(bluetooth, ble, tor, lan);
				}
				return asList(bluetooth, tor, lan);
			}

			@Override
			public Collection<SimplexPluginFactory> getSimplexFactories() {
				List<SimplexPluginFactory> simplex = new ArrayList<>();
				simplex.add(mailbox);
				simplex.add(drive);
				return simplex;
			}

			@Override
			public boolean shouldPoll() {
				return true;
			}

			@Override
			public Map<TransportId, List<TransportId>> getTransportPreferences() {
				// Prefer LAN to Bluetooth Classic and to Bluetooth LE
				Map<TransportId, List<TransportId>> prefs = new HashMap<>();
				prefs.put(BluetoothConstants.ID,
						singletonList(LanTcpConstants.ID));
				prefs.put(BleConstants.ID, singletonList(LanTcpConstants.ID));
				return prefs;
			}
		};
		return pluginConfig;
	}

	@Provides
	@Singleton
	DevConfig provideDevConfig(Application app) {
		@NotNullByDefault
		DevConfig devConfig = () ->
				AndroidUtils.getLogcatFile(app.getApplicationContext());
		return devConfig;
	}

	@Provides
	TestAvatarCreator provideTestAvatarCreator(
			TestAvatarCreatorImpl testAvatarCreator) {
		return testAvatarCreator;
	}

	@Provides
	SharedPreferences provideSharedPreferences(Application app) {
		// FIXME unify this with getDefaultSharedPreferences()
		return app.getSharedPreferences("db", MODE_PRIVATE);
	}

	@Provides
	@Singleton
	AndroidNotificationManager provideAndroidNotificationManager(
			LifecycleManager lifecycleManager, EventBus eventBus,
			AndroidNotificationManagerImpl notificationManager) {
		lifecycleManager.registerService(notificationManager);
		eventBus.addListener(notificationManager);
		return notificationManager;
	}

	@Provides
	@Singleton
	ScreenFilterMonitor provideScreenFilterMonitor(
			LifecycleManager lifecycleManager,
			ScreenFilterMonitorImpl screenFilterMonitor) {
		if (SDK_INT <= 29) {
			// this keeps track of installed apps and does not work on API 30+
			lifecycleManager.registerService(screenFilterMonitor);
		}
		return screenFilterMonitor;
	}

	@Provides
	@Singleton
	NetworkUsageMetrics provideNetworkUsageMetrics(
			LifecycleManager lifecycleManager) {
		NetworkUsageMetrics networkUsageMetrics = new NetworkUsageMetricsImpl();
		lifecycleManager.registerService(networkUsageMetrics);
		return networkUsageMetrics;
	}

	@Provides
	@Singleton
	DozeWatchdog provideDozeWatchdog(LifecycleManager lifecycleManager) {
		DozeWatchdogImpl dozeWatchdog = new DozeWatchdogImpl(application);
		lifecycleManager.registerService(dozeWatchdog);
		return dozeWatchdog;
	}

	@Provides
	@Singleton
	LockManager provideLockManager(LifecycleManager lifecycleManager,
			EventBus eventBus, LockManagerImpl lockManager) {
		lifecycleManager.registerService(lockManager);
		eventBus.addListener(lockManager);
		return lockManager;
	}

	@Provides
	@Singleton
	RecentEmoji provideRecentEmoji(LifecycleManager lifecycleManager,
			RecentEmojiImpl recentEmoji) {
		lifecycleManager.registerOpenDatabaseHook(recentEmoji);
		return recentEmoji;
	}

	@Provides
	FeatureFlags provideFeatureFlags() {
		return new FeatureFlags() {

			@Override
			public boolean shouldEnableImageAttachments() {
				return true;
			}

			@Override
			public boolean shouldEnableProfilePictures() {
				return true;
			}

			@Override
			public boolean shouldEnableDisappearingMessages() {
				return true;
			}

			@Override
			public boolean shouldEnablePrivateGroupsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableForumsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableBlogsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableHybridPqKeyAgreement() {
				return false;
			}

			@Override
			public boolean shouldEnableGroupSenderKeys() {
				return false;
			}

			@Override
			public boolean shouldEnableSealedEnvelopeRelay() {
				return false;
			}

			@Override
			public boolean shouldEnableBleTransport() {
				return IS_DEBUG_BUILD;
			}
		};
	}
}
