package org.chagolchana.noconnect.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager.StartResult;
import org.chagolchana.chagol.api.system.AndroidExecutor;
import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.navdrawer.NavDrawerActivity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.inject.Inject;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.support.v4.app.NotificationCompat.CATEGORY_SERVICE;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;
import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;
import static java.util.logging.Level.WARNING;
import static org.chagolchana.chagol.api.lifecycle.LifecycleManager.StartResult.ALREADY_RUNNING;
import static org.chagolchana.chagol.api.lifecycle.LifecycleManager.StartResult.SUCCESS;

public class NoConnectService extends Service {

	private static final int ONGOING_NOTIFICATION_ID = 1;
	private static final int FAILURE_NOTIFICATION_ID = 2;

	private static final Logger LOG =
			Logger.getLogger(NoConnectService.class.getName());

	private final AtomicBoolean created = new AtomicBoolean(false);
	private final Binder binder = new NoConnectBinder();

	@Inject
	protected DatabaseConfig databaseConfig;
	// Fields that are accessed from background threads must be volatile
	@Inject
	protected volatile LifecycleManager lifecycleManager;
	@Inject
	protected volatile AndroidExecutor androidExecutor;
	private volatile boolean started = false;

	@Override
	public void onCreate() {
		super.onCreate();

		NoConnectApplication application = (NoConnectApplication) getApplication();
		application.getApplicationComponent().inject(this);

		LOG.info("Created");
		if (created.getAndSet(true)) {
			LOG.info("Already created");
			stopSelf();
			return;
		}
		if (databaseConfig.getEncryptionKey() == null) {
			LOG.info("No database key");
			stopSelf();
			return;
		}
		// Show an ongoing notification that the service is running
		NotificationCompat.Builder b = new NotificationCompat.Builder(this);
		b.setSmallIcon(R.drawable.notification_ongoing);
		b.setColor(ContextCompat.getColor(this, R.color.nc_primary));
		b.setContentTitle(getText(R.string.ongoing_notification_title));
		b.setContentText(getText(R.string.ongoing_notification_text));
		b.setWhen(0); // Don't show the time
		b.setOngoing(true);
		Intent i = new Intent(this, NavDrawerActivity.class);
		i.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
		b.setContentIntent(PendingIntent.getActivity(this, 0, i, 0));
		if (Build.VERSION.SDK_INT >= 21) {
			b.setCategory(CATEGORY_SERVICE);
			b.setVisibility(VISIBILITY_SECRET);
		}
		b.setPriority(PRIORITY_MIN);
		startForeground(ONGOING_NOTIFICATION_ID, b.build());
		// Start the services in a background thread
		new Thread() {
			@Override
			public void run() {
				String nickname = databaseConfig.getLocalAuthorName();
				StartResult result = lifecycleManager.startServices(nickname);
				if (result == SUCCESS) {
					started = true;
				} else if (result == ALREADY_RUNNING) {
					LOG.info("Already running");
					stopSelf();
				} else {
					if (LOG.isLoggable(WARNING))
						LOG.warning("Startup failed: " + result);
					showStartupFailureNotification(result);
					stopSelf();
				}
			}
		}.start();
	}

	private void showStartupFailureNotification(final StartResult result) {
		androidExecutor.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				NotificationCompat.Builder b =
						new NotificationCompat.Builder(NoConnectService.this);
				b.setSmallIcon(android.R.drawable.stat_notify_error);
				b.setContentTitle(getText(
						R.string.startup_failed_notification_title));
				b.setContentText(getText(
						R.string.startup_failed_notification_text));
				Intent i = new Intent(NoConnectService.this,
						StartupFailureActivity.class);
				i.setFlags(FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("noconnect.START_RESULT", result);
				i.putExtra("noconnect.FAILURE_NOTIFICATION_ID",
						FAILURE_NOTIFICATION_ID);
				b.setContentIntent(PendingIntent.getActivity(NoConnectService.this,
						0, i, FLAG_UPDATE_CURRENT));
				Object o = getSystemService(NOTIFICATION_SERVICE);
				NotificationManager nm = (NotificationManager) o;
				nm.notify(FAILURE_NOTIFICATION_ID, b.build());
				// Bring the dashboard to the front to clear the back stack
				i = new Intent(NoConnectService.this, NavDrawerActivity.class);
				i.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("noconnect.STARTUP_FAILED", true);
				startActivity(i);
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY; // Don't restart automatically if killed
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LOG.info("Destroyed");
		stopForeground(true);
		// Stop the services in a background thread
		new Thread() {
			@Override
			public void run() {
				if (started) lifecycleManager.stopServices();
			}
		}.start();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		LOG.warning("Memory is low");
		// FIXME: Work out what to do about it
	}

	/**
	 * Waits for all services to start before returning.
	 */
	public void waitForStartup() throws InterruptedException {
		lifecycleManager.waitForStartup();
	}

	/**
	 * Waits for all services to stop before returning.
	 */
	public void waitForShutdown() throws InterruptedException {
		lifecycleManager.waitForShutdown();
	}

	/**
	 * Starts the shutdown process.
	 */
	public void shutdown() {
		stopSelf(); // This will call onDestroy()
	}

	public class NoConnectBinder extends Binder {

		/**
		 * Returns the bound service.
		 */
		public NoConnectService getService() {
			return NoConnectService.this;
		}
	}

	public static class NoConnectServiceConnection implements ServiceConnection {

		private final CountDownLatch binderLatch = new CountDownLatch(1);

		private volatile IBinder binder = null;

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			this.binder = binder;
			binderLatch.countDown();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		/**
		 * Waits for the service to connect and returns its binder.
		 */
		public IBinder waitForBinder() throws InterruptedException {
			binderLatch.await();
			return binder;
		}
	}
}
