package org.chagolchana.chagol.account;

import android.app.Application;
import android.content.SharedPreferences;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.identity.IdentityManager;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.Localizer;
import org.chagolchana.noconnect.android.util.UiUtils;

import javax.inject.Inject;

class NoConnectAccountManager extends AndroidAccountManager {

	@Inject
	NoConnectAccountManager(DatabaseConfig databaseConfig, CryptoComponent crypto,
			IdentityManager identityManager, SharedPreferences prefs,
			Application app) {
		super(databaseConfig, crypto, identityManager, prefs, app);
	}

	@Override
	public void deleteAccount() {
		synchronized (stateChangeLock) {
			super.deleteAccount();
			Localizer.reinitialize();
			UiUtils.setTheme(appContext,
					appContext.getString(R.string.pref_theme_system_value));
		}
	}
}
