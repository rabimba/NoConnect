package org.chagolchana.noconnect.android.settings;

import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.settings.Settings;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.util.LogUtils.logDuration;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.LogUtils.now;

/**
 * A custom PreferenceDataStore that stores settings in NoConnect's encrypted DB.
 */
@NotNullByDefault
class SettingsStore extends PreferenceDataStore {

	private final static Logger LOG = getLogger(SettingsStore.class.getName());

	private final SettingsManager settingsManager;
	private final Executor dbExecutor;
	private final String namespace;

	SettingsStore(SettingsManager settingsManager,
			Executor dbExecutor,
			String namespace) {
		this.settingsManager = settingsManager;
		this.dbExecutor = dbExecutor;
		this.namespace = namespace;
	}

	@Override
	public void putBoolean(String key, boolean value) {
		if (LOG.isLoggable(INFO))
			LOG.info("Store bool setting: " + key + "=" + value);
		Settings s = new Settings();
		s.putBoolean(key, value);
		storeSettings(s);
	}

	@Override
	public void putInt(String key, int value) {
		if (LOG.isLoggable(INFO))
			LOG.info("Store int setting: " + key + "=" + value);
		Settings s = new Settings();
		s.putInt(key, value);
		storeSettings(s);
	}

	@Override
	public void putString(String key, @Nullable String value) {
		if (LOG.isLoggable(INFO))
			LOG.info("Store string setting: " + key + "=" + value);
		Settings s = new Settings();
		s.put(key, value);
		storeSettings(s);
	}

	private void storeSettings(Settings s) {
		dbExecutor.execute(() -> {
			try {
				long start = now();
				settingsManager.mergeSettings(s, namespace);
				logDuration(LOG, "Merging " + namespace + " settings", start);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

}
