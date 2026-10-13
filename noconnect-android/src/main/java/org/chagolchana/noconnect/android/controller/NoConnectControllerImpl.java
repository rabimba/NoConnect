package org.chagolchana.noconnect.android.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;

import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.chagol.api.account.AccountManager;
import org.chagolchana.chagol.api.db.DatabaseExecutor;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.settings.Settings;
import org.chagolchana.chagol.api.settings.SettingsManager;
import org.chagolchana.noconnect.android.NoConnectApplication;
import org.chagolchana.noconnect.android.NoConnectService;
import org.chagolchana.noconnect.android.NoConnectService.NoConnectServiceConnection;
import org.chagolchana.noconnect.android.controller.handler.ResultHandler;
import org.chagolchana.noconnect.api.android.DozeWatchdog;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.CallSuper;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.dontkillme.DozeUtils.needsDozeWhitelisting;
import static org.chagolchana.chagol.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.noconnect.android.settings.SettingsFragment.SETTINGS_NAMESPACE;

@NotNullByDefault
public class NoConnectControllerImpl implements NoConnectController {

	private static final Logger LOG =
			getLogger(NoConnectControllerImpl.class.getName());

	public static final String DOZE_ASK_AGAIN = "dozeAskAgain";

	private final NoConnectServiceConnection serviceConnection;
	private final AccountManager accountManager;
	private final LifecycleManager lifecycleManager;
	private final Executor databaseExecutor;
	private final SettingsManager settingsManager;
	private final DozeWatchdog dozeWatchdog;
	private final AndroidWakeLockManager wakeLockManager;
	private final Activity activity;

	private boolean bound = false;

	@Inject
	NoConnectControllerImpl(NoConnectServiceConnection serviceConnection,
			AccountManager accountManager,
			LifecycleManager lifecycleManager,
			@DatabaseExecutor Executor databaseExecutor,
			SettingsManager settingsManager,
			DozeWatchdog dozeWatchdog,
			AndroidWakeLockManager wakeLockManager,
			Activity activity) {
		this.serviceConnection = serviceConnection;
		this.accountManager = accountManager;
		this.lifecycleManager = lifecycleManager;
		this.databaseExecutor = databaseExecutor;
		this.settingsManager = settingsManager;
		this.dozeWatchdog = dozeWatchdog;
		this.wakeLockManager = wakeLockManager;
		this.activity = activity;
	}

	@Override
	@CallSuper
	public void onActivityCreate(Activity activity) {
		if (accountManager.hasDatabaseKey()) startAndBindService();
	}

	@Override
	public void onActivityStart() {
	}

	@Override
	public void onActivityStop() {
	}

	@Override
	@CallSuper
	public void onActivityDestroy() {
		unbindService();
	}

	@Override
	public void startAndBindService() {
		activity.startService(new Intent(activity, NoConnectService.class));
		bound = activity.bindService(new Intent(activity, NoConnectService.class),
				serviceConnection, 0);
	}

	@Override
	public boolean accountSignedIn() {
		return accountManager.hasDatabaseKey() &&
				lifecycleManager.getLifecycleState().isAfter(STARTING_SERVICES);
	}

	@Override
	public void hasDozed(ResultHandler<Boolean> handler) {
		NoConnectApplication app = (NoConnectApplication) activity.getApplication();
		if (app.isInstrumentationTest() || !dozeWatchdog.getAndResetDozeFlag()
				|| !needsDozeWhitelisting(activity)) {
			handler.onResult(false);
			return;
		}
		databaseExecutor.execute(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				handler.onResult(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@Override
	public void doNotAskAgainForDozeWhiteListing() {
		databaseExecutor.execute(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(DOZE_ASK_AGAIN, false);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@Override
	public void signOut(ResultHandler<Void> handler, boolean deleteAccount) {
		wakeLockManager.executeWakefully(() -> {
			try {
				// Wait for the service to finish starting up
				IBinder binder = serviceConnection.waitForBinder();
				NoConnectService service =
						((NoConnectService.NoConnectBinder) binder).getService();
				service.waitForStartup();
				// Shut down the service and wait for it to shut down
				LOG.info("Shutting down service");
				service.shutdown(true);
				service.waitForShutdown();
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for service");
			} finally {
				if (deleteAccount) accountManager.deleteAccount();
			}
			handler.onResult(null);
		}, "SignOut");
	}

	@Override
	public void deleteAccount() {
		accountManager.deleteAccount();
	}

	private void unbindService() {
		if (bound) activity.unbindService(serviceConnection);
	}

}
