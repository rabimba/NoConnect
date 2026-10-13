package org.chagolchana.noconnect.android.settings;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.widget.Toast;

import org.chagolchana.chagol.api.FeatureFlags;
import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.TransactionManager;
import org.chagolchana.chagol.api.event.Event;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventListener;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.chagol.api.identity.LocalAuthor;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.plugin.BluetoothConstants;
import org.chagolchana.chagol.api.plugin.LanTcpConstants;
import org.chagolchana.chagol.api.plugin.TorConstants;
import org.chagolchana.chagol.api.settings.Settings;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.chagol.api.settings.event.SettingsUpdatedEvent;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.attachment.UnsupportedMimeTypeException;
import org.chagolchana.noconnect.android.attachment.media.ImageCompressor;
import org.chagolchana.noconnect.android.viewmodel.DbViewModel;
import org.chagolchana.noconnect.api.avatar.AvatarManager;
import org.chagolchana.noconnect.api.identity.AuthorInfo;
import org.chagolchana.noconnect.api.identity.AuthorManager;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;
import org.chagolchana.onionwrapper.CircumventionProvider;
import org.chagolchana.onionwrapper.LocationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.AnyThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static android.widget.Toast.LENGTH_LONG;
import static java.util.Arrays.asList;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.util.AndroidUtils.getSupportedImageContentTypes;
import static org.chagolchana.chagol.util.LogUtils.logDuration;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.LogUtils.now;
import static org.chagolchana.noconnect.android.settings.SecurityFragment.PREF_SCREEN_LOCK;
import static org.chagolchana.noconnect.android.settings.SecurityFragment.PREF_SCREEN_LOCK_TIMEOUT;
import static org.chagolchana.noconnect.android.settings.SettingsFragment.SETTINGS_NAMESPACE;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
class SettingsViewModel extends DbViewModel implements EventListener {

	private final static Logger LOG =
			getLogger(SettingsViewModel.class.getName());

	static final String BT_NAMESPACE =
			BluetoothConstants.ID.getString();
	static final String WIFI_NAMESPACE = LanTcpConstants.ID.getString();
	static final String TOR_NAMESPACE = TorConstants.ID.getString();

	private final SettingsManager settingsManager;
	private final IdentityManager identityManager;
	private final EventBus eventBus;
	private final AvatarManager avatarManager;
	private final AuthorManager authorManager;
	private final ImageCompressor imageCompressor;
	private final Executor ioExecutor;
	private final FeatureFlags featureFlags;

	final SettingsStore settingsStore;
	final TorSummaryProvider torSummaryProvider;
	final ConnectionsManager connectionsManager;
	final NotificationsManager notificationsManager;

	private volatile Settings settings;

	private final MutableLiveData<OwnIdentityInfo> ownIdentityInfo =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> screenLockEnabled =
			new MutableLiveData<>();
	private final MutableLiveData<String> screenLockTimeout =
			new MutableLiveData<>();

	@Inject
	SettingsViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager,
			IdentityManager identityManager,
			EventBus eventBus,
			AvatarManager avatarManager,
			AuthorManager authorManager,
			ImageCompressor imageCompressor,
			LocationUtils locationUtils,
			CircumventionProvider circumventionProvider,
			@IoExecutor Executor ioExecutor,
			FeatureFlags featureFlags) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.settingsManager = settingsManager;
		this.identityManager = identityManager;
		this.eventBus = eventBus;
		this.imageCompressor = imageCompressor;
		this.avatarManager = avatarManager;
		this.authorManager = authorManager;
		this.ioExecutor = ioExecutor;
		this.featureFlags = featureFlags;
		settingsStore = new SettingsStore(settingsManager, dbExecutor,
				SETTINGS_NAMESPACE);
		torSummaryProvider = new TorSummaryProvider(getApplication(),
				locationUtils, circumventionProvider);
		connectionsManager =
				new ConnectionsManager(settingsManager, dbExecutor);
		notificationsManager = new NotificationsManager(getApplication(),
				settingsManager, dbExecutor);

		eventBus.addListener(this);
		loadSettings();
		if (shouldEnableProfilePictures()) loadOwnIdentityInfo();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}

	private void loadSettings() {
		runOnDbThread(() -> {
			try {
				long start = now();
				settings = settingsManager.getSettings(SETTINGS_NAMESPACE);
				updateSettings(settings);
				connectionsManager.updateBtSetting(
						settingsManager.getSettings(BT_NAMESPACE));
				connectionsManager.updateWifiSettings(
						settingsManager.getSettings(WIFI_NAMESPACE));
				connectionsManager.updateTorSettings(
						settingsManager.getSettings(TOR_NAMESPACE));
				logDuration(LOG, "Loading settings", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	boolean shouldEnableProfilePictures() {
		return featureFlags.shouldEnableProfilePictures();
	}

	private void loadOwnIdentityInfo() {
		runOnDbThread(() -> {
			try {
				LocalAuthor localAuthor = identityManager.getLocalAuthor();
				AuthorInfo authorInfo = authorManager.getMyAuthorInfo();
				ownIdentityInfo.postValue(
						new OwnIdentityInfo(localAuthor, authorInfo));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
			String namespace = s.getNamespace();
			if (namespace.equals(SETTINGS_NAMESPACE)) {
				LOG.info("Settings updated");
				settings = s.getSettings();
				updateSettings(settings);
			} else if (namespace.equals(BT_NAMESPACE)) {
				LOG.info("Bluetooth settings updated");
				connectionsManager.updateBtSetting(s.getSettings());
			} else if (namespace.equals(WIFI_NAMESPACE)) {
				LOG.info("Wifi settings updated");
				connectionsManager.updateWifiSettings(s.getSettings());
			} else if (namespace.equals(TOR_NAMESPACE)) {
				LOG.info("Tor settings updated");
				connectionsManager.updateTorSettings(s.getSettings());
			}
		}
	}

	@AnyThread
	private void updateSettings(Settings settings) {
		screenLockEnabled.postValue(settings.getBoolean(PREF_SCREEN_LOCK,
				false));
		int defaultTimeout = Integer.parseInt(getApplication()
				.getString(R.string.pref_lock_timeout_value_default));
		screenLockTimeout.postValue(String.valueOf(
				settings.getInt(PREF_SCREEN_LOCK_TIMEOUT, defaultTimeout)
		));
		notificationsManager.updateSettings(settings);
	}

	void setAvatar(Uri uri) {
		ioExecutor.execute(() -> {
			try {
				trySetAvatar(uri);
			} catch (IOException e) {
				logException(LOG, WARNING, e);
				onSetAvatarFailed();
			}
		});
	}

	private void trySetAvatar(Uri uri) throws IOException {
		ContentResolver contentResolver =
				getApplication().getContentResolver();
		String contentType = contentResolver.getType(uri);
		if (contentType == null) throw new IOException("null content type");
		if (!asList(getSupportedImageContentTypes()).contains(contentType)) {
			throw new UnsupportedMimeTypeException(contentType, uri);
		}
		InputStream is;
		try {
			is = contentResolver.openInputStream(uri);
			if (is == null) throw new IOException(
					"ContentResolver returned null when opening InputStream");
		} catch (SecurityException e) {
			throw new IOException(e);
		}
		InputStream compressed = imageCompressor.compressImage(is, contentType);

		runOnDbThread(() -> {
			try {
				avatarManager.addAvatar(ImageCompressor.MIME_TYPE, compressed);
				loadOwnIdentityInfo();
			} catch (IOException | DbException e) {
				logException(LOG, WARNING, e);
				onSetAvatarFailed();
			}
		});
	}

	@AnyThread
	private void onSetAvatarFailed() {
		androidExecutor.runOnUiThread(() -> Toast.makeText(getApplication(),
				R.string.change_profile_picture_failed_message, LENGTH_LONG)
				.show());
	}

	LiveData<OwnIdentityInfo> getOwnIdentityInfo() {
		return ownIdentityInfo;
	}

	LiveData<Boolean> getScreenLockEnabled() {
		return screenLockEnabled;
	}

	LiveData<String> getScreenLockTimeout() {
		return screenLockTimeout;
	}

}
