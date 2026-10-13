package org.chagolchana.noconnect.android.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.CallSuper;

import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.noconnect.android.NoConnectService;
import org.chagolchana.noconnect.android.NoConnectService.NoConnectServiceConnection;
import org.chagolchana.noconnect.android.controller.handler.ResultHandler;

import java.util.logging.Logger;

import javax.inject.Inject;

public class NoConnectControllerImpl implements NoConnectController {

	private static final Logger LOG =
			Logger.getLogger(NoConnectControllerImpl.class.getName());

	private final NoConnectServiceConnection serviceConnection;
	private final DatabaseConfig databaseConfig;
	private final Activity activity;

	private boolean bound = false;

	@Inject
	NoConnectControllerImpl(NoConnectServiceConnection serviceConnection,
			DatabaseConfig databaseConfig, Activity activity) {
		this.serviceConnection = serviceConnection;
		this.databaseConfig = databaseConfig;
		this.activity = activity;
	}

	@Override
	@CallSuper
	public void onActivityCreate(Activity activity) {
		if (databaseConfig.getEncryptionKey() != null) startAndBindService();
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
	public boolean hasEncryptionKey() {
		return databaseConfig.getEncryptionKey() != null;
	}

	@Override
	public void signOut(final ResultHandler<Void> eventHandler) {
		new Thread() {
			@Override
			public void run() {
				try {
					// Wait for the service to finish starting up
					IBinder binder = serviceConnection.waitForBinder();
					NoConnectService service =
							((NoConnectService.NoConnectBinder) binder).getService();
					service.waitForStartup();
					// Shut down the service and wait for it to shut down
					LOG.info("Shutting down service");
					service.shutdown();
					service.waitForShutdown();
				} catch (InterruptedException e) {
					LOG.warning("Interrupted while waiting for service");
				}
				eventHandler.onResult(null);
			}
		}.start();
	}

	private void unbindService() {
		if (bound) activity.unbindService(serviceConnection);
	}

}
