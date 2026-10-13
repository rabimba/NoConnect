package org.chagolchana.noconnect.android.settings;

import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import androidx.annotation.Nullable;

import static org.chagolchana.chagol.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static org.chagolchana.chagol.api.plugin.TorConstants.PREF_TOR_MOBILE;
import static org.chagolchana.chagol.api.plugin.TorConstants.PREF_TOR_NETWORK;
import static org.chagolchana.chagol.api.plugin.TorConstants.PREF_TOR_ONLY_WHEN_CHARGING;
import static org.chagolchana.noconnect.android.settings.ConnectionsFragment.PREF_KEY_BLUETOOTH;
import static org.chagolchana.noconnect.android.settings.ConnectionsFragment.PREF_KEY_TOR_ENABLE;
import static org.chagolchana.noconnect.android.settings.ConnectionsFragment.PREF_KEY_TOR_MOBILE_DATA;
import static org.chagolchana.noconnect.android.settings.ConnectionsFragment.PREF_KEY_TOR_NETWORK;
import static org.chagolchana.noconnect.android.settings.ConnectionsFragment.PREF_KEY_TOR_ONLY_WHEN_CHARGING;
import static org.chagolchana.noconnect.android.settings.ConnectionsFragment.PREF_KEY_WIFI;

@NotNullByDefault
class ConnectionsStore extends SettingsStore {

	ConnectionsStore(
			SettingsManager settingsManager,
			Executor dbExecutor,
			String namespace) {
		super(settingsManager, dbExecutor, namespace);
	}

	@Override
	public void putBoolean(String key, boolean value) {
		String newKey;
		// translate between Android UI pref keys and chagol keys
		switch (key) {
			case PREF_KEY_BLUETOOTH:
			case PREF_KEY_WIFI:
			case PREF_KEY_TOR_ENABLE:
				newKey = PREF_PLUGIN_ENABLE;
				break;
			case PREF_KEY_TOR_MOBILE_DATA:
				newKey = PREF_TOR_MOBILE;
				break;
			case PREF_KEY_TOR_ONLY_WHEN_CHARGING:
				newKey = PREF_TOR_ONLY_WHEN_CHARGING;
				break;
			default:
				throw new AssertionError();
		}
		super.putBoolean(newKey, value);
	}

	@Override
	public void putString(String key, @Nullable String value) {
		// translate between Android UI pref keys and chagol keys
		if (key.equals(PREF_KEY_TOR_NETWORK)) {
			super.putString(PREF_TOR_NETWORK, value);
		} else {
			throw new AssertionError(key);
		}
	}

}
