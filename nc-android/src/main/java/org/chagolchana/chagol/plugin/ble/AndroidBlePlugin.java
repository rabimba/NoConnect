package org.chagolchana.chagol.plugin.ble;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.Pair;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.data.BdfList;
import org.chagolchana.chagol.api.keyagreement.KeyAgreementListener;
import org.chagolchana.chagol.api.plugin.ConnectionHandler;
import org.chagolchana.chagol.api.plugin.PluginCallback;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPlugin;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.chagol.api.rendezvous.KeyMaterialSource;
import org.chagolchana.chagol.api.rendezvous.RendezvousEndpoint;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.api.io.TimeoutMonitor;
import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.nullsafety.MethodsNotNullByDefault;
import org.chagolchana.nullsafety.ParametersNotNullByDefault;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.api.plugin.BleConstants.ID;
import static org.chagolchana.chagol.api.plugin.BleConstants.PROP_SECRET;
import static org.chagolchana.chagol.api.plugin.BleConstants.SECRET_BYTES;
import static org.chagolchana.chagol.api.plugin.BleConstants.SERVICE_UUID;
import static org.chagolchana.chagol.api.plugin.Plugin.State.ACTIVE;
import static org.chagolchana.chagol.api.plugin.Plugin.State.INACTIVE;
import static org.chagolchana.chagol.api.plugin.Plugin.State.STARTING_STOPPING;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.EPOCH_MS;
import static org.chagolchana.chagol.plugin.ble.BleRendezvousTokens.epochFor;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.chagol.util.StringUtils.fromHexString;
import static org.chagolchana.chagol.util.StringUtils.toHexString;

/**
 * EXPERIMENTAL Bluetooth LE transport (Android 10+). Each device advertises
 * a rotating token derived from its rendezvous secret, which only its
 * contacts know, plus the L2CAP channel it listens on. Contacts that see a
 * matching token connect over an insecure L2CAP channel; BTP authenticates
 * and encrypts the stream. See docs/design/ble-discovery.md.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
@SuppressLint("MissingPermission") // Checked in hasPermissions()
class AndroidBlePlugin implements DuplexPlugin {

	private static final Logger LOG =
			getLogger(AndroidBlePlugin.class.getName());

	private static final int POLLING_INTERVAL_MS = 60 * 1000;
	// Forget sightings older than this
	private static final long SIGHTING_EXPIRY_MS = 2 * EPOCH_MS;

	private static final ParcelUuid SERVICE =
			new ParcelUuid(UUID.fromString(SERVICE_UUID));

	private final Application app;
	private final Executor ioExecutor;
	private final AndroidWakeLockManager wakeLockManager;
	private final TimeoutMonitor timeoutMonitor;
	private final SecureRandom secureRandom;
	private final Clock clock;
	private final PluginCallback callback;
	private final BleRendezvousTokens tokens;
	private final long maxLatency;
	private final int maxIdleTime;
	private final Handler handler = new Handler(Looper.getMainLooper());

	// Token (hex) -> where it was seen
	private final Map<String, Sighting> sightings = new ConcurrentHashMap<>();

	private final Object lock = new Object();
	@GuardedBy("lock")
	private boolean started = false, up = false;
	@GuardedBy("lock")
	@Nullable
	private BluetoothServerSocket serverSocket = null;
	@GuardedBy("lock")
	@Nullable
	private SecretKey localSecret = null;

	private volatile State state = STARTING_STOPPING;

	AndroidBlePlugin(Application app, Executor ioExecutor,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor, SecureRandom secureRandom,
			Clock clock, CryptoComponent crypto, PluginCallback callback,
			long maxLatency, int maxIdleTime) {
		this.app = app;
		this.ioExecutor = ioExecutor;
		this.wakeLockManager = wakeLockManager;
		this.timeoutMonitor = timeoutMonitor;
		this.secureRandom = secureRandom;
		this.clock = clock;
		this.callback = callback;
		this.maxLatency = maxLatency;
		this.maxIdleTime = maxIdleTime;
		tokens = new BleRendezvousTokens(crypto);
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return maxLatency;
	}

	@Override
	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	@Override
	public void start() {
		synchronized (lock) {
			if (started) return;
			started = true;
			localSecret = loadOrCreateSecret();
		}
		app.registerReceiver(adapterReceiver,
				new IntentFilter(ACTION_STATE_CHANGED));
		bringUpIfPossible();
	}

	@Override
	public void stop() {
		synchronized (lock) {
			if (!started) return;
			started = false;
		}
		try {
			app.unregisterReceiver(adapterReceiver);
		} catch (IllegalArgumentException e) {
			// Not registered
		}
		tearDown();
		setState(STARTING_STOPPING);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public int getReasonsDisabled() {
		return 0;
	}

	@Override
	public boolean shouldPoll() {
		return true;
	}

	@Override
	public int getPollingInterval() {
		return POLLING_INTERVAL_MS;
	}

	@Override
	public void poll(Collection<Pair<TransportProperties, ConnectionHandler>>
			properties) {
		// Permissions may have been granted since we last tried
		if (state != ACTIVE) bringUpIfPossible();
		if (state != ACTIVE) return;
		pruneSightings();
		for (Pair<TransportProperties, ConnectionHandler> p : properties) {
			if (findNearby(p.getFirst()) == null) continue;
			ioExecutor.execute(() -> {
				DuplexTransportConnection c = createConnection(p.getFirst());
				if (c != null) p.getSecond().handleConnection(c);
			});
		}
	}

	@Override
	@Nullable
	public DuplexTransportConnection createConnection(TransportProperties p) {
		if (state != ACTIVE || SDK_INT < 29) return null;
		Sighting s = findNearby(p);
		if (s == null) return null;
		try {
			BluetoothSocket socket =
					s.device.createInsecureL2capChannel(s.psm);
			socket.connect();
			LOG.info("Connected over BLE L2CAP");
			return new BleTransportConnection(this, wakeLockManager,
					timeoutMonitor, socket);
		} catch (IOException | SecurityException e) {
			if (LOG.isLoggable(INFO)) LOG.info("BLE connection failed: " + e);
			return null;
		}
	}

	@Override
	public boolean supportsKeyAgreement() {
		return false;
	}

	@Override
	@Nullable
	public KeyAgreementListener createKeyAgreementListener(
			byte[] localCommitment) {
		return null;
	}

	@Override
	@Nullable
	public DuplexTransportConnection createKeyAgreementConnection(
			byte[] remoteCommitment, BdfList descriptor) {
		return null;
	}

	@Override
	public boolean supportsRendezvous() {
		return false;
	}

	@Override
	@Nullable
	public RendezvousEndpoint createRendezvousEndpoint(KeyMaterialSource k,
			boolean alice, ConnectionHandler incoming) {
		return null;
	}

	/**
	 * Returns where a contact with the given properties was seen in the
	 * current, previous or next epoch, or null if they haven't been seen.
	 */
	@Nullable
	private Sighting findNearby(TransportProperties p) {
		String secretHex = p.get(PROP_SECRET);
		if (secretHex == null) return null;
		SecretKey secret;
		try {
			byte[] b = fromHexString(secretHex);
			if (b.length != SECRET_BYTES) return null;
			secret = new SecretKey(b);
		} catch (FormatException e) {
			return null;
		}
		long epoch = epochFor(clock.currentTimeMillis());
		for (long e = epoch - 1; e <= epoch + 1; e++) {
			Sighting s = sightings.get(toHexString(tokens.deriveToken(secret,
					e)));
			if (s != null) return s;
		}
		return null;
	}

	private void pruneSightings() {
		long now = clock.currentTimeMillis();
		Iterator<Sighting> it = sightings.values().iterator();
		while (it.hasNext()) {
			if (now - it.next().seen > SIGHTING_EXPIRY_MS) it.remove();
		}
	}

	private SecretKey loadOrCreateSecret() {
		TransportProperties local = callback.getLocalProperties();
		String hex = local.get(PROP_SECRET);
		if (hex != null) {
			try {
				byte[] b = fromHexString(hex);
				if (b.length == SECRET_BYTES) return new SecretKey(b);
			} catch (FormatException e) {
				LOG.warning("Invalid stored BLE secret, creating a new one");
			}
		}
		byte[] b = new byte[SECRET_BYTES];
		secureRandom.nextBytes(b);
		TransportProperties p = new TransportProperties();
		p.put(PROP_SECRET, toHexString(b));
		callback.mergeLocalProperties(p);
		return new SecretKey(b);
	}

	private void bringUpIfPossible() {
		synchronized (lock) {
			if (!started || up) return;
			BluetoothAdapter adapter = getAdapter();
			if (SDK_INT < 29 || adapter == null || !adapter.isEnabled()
					|| !hasPermissions()) {
				setState(INACTIVE);
				return;
			}
			try {
				BluetoothServerSocket ss =
						adapter.listenUsingInsecureL2capChannel();
				serverSocket = ss;
				int psm = ss.getPsm();
				ioExecutor.execute(() -> acceptConnections(ss));
				up = true;
				startAdvertising(adapter, psm);
				startScanning(adapter);
				scheduleTokenRotation();
				if (LOG.isLoggable(INFO)) LOG.info("BLE up, PSM " + psm);
				setState(ACTIVE);
			} catch (IOException | SecurityException e) {
				logException(LOG, WARNING, e);
				tearDownLocked();
				setState(INACTIVE);
			}
		}
	}

	private void tearDown() {
		synchronized (lock) {
			tearDownLocked();
		}
		setState(INACTIVE);
	}

	@GuardedBy("lock")
	private void tearDownLocked() {
		handler.removeCallbacksAndMessages(null);
		BluetoothAdapter adapter = getAdapter();
		if (adapter != null && hasPermissions()) {
			try {
				BluetoothLeAdvertiser advertiser =
						adapter.getBluetoothLeAdvertiser();
				if (advertiser != null) {
					advertiser.stopAdvertising(advertiseCallback);
				}
				BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
				if (scanner != null) scanner.stopScan(scanCallback);
			} catch (IllegalStateException | SecurityException e) {
				// Adapter turned off
			}
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				logException(LOG, WARNING, e);
			}
			serverSocket = null;
		}
		sightings.clear();
		up = false;
	}

	private void acceptConnections(BluetoothServerSocket ss) {
		while (true) {
			BluetoothSocket socket;
			try {
				socket = ss.accept();
			} catch (IOException e) {
				LOG.info("BLE server socket closed");
				return;
			}
			try {
				LOG.info("Incoming BLE L2CAP connection");
				callback.handleConnection(new BleTransportConnection(this,
						wakeLockManager, timeoutMonitor, socket));
			} catch (IOException e) {
				logException(LOG, WARNING, e);
			}
		}
	}

	@GuardedBy("lock")
	private void startAdvertising(BluetoothAdapter adapter, int psm) {
		BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
		if (advertiser == null || localSecret == null) {
			LOG.warning("BLE advertising not supported");
			return;
		}
		byte[] token = tokens.deriveToken(localSecret,
				epochFor(clock.currentTimeMillis()));
		AdvertiseSettings settings = new AdvertiseSettings.Builder()
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
				.setConnectable(true)
				.setTimeout(0)
				.build();
		AdvertiseData data = new AdvertiseData.Builder()
				.setIncludeDeviceName(false)
				.setIncludeTxPowerLevel(false)
				.addServiceData(SERVICE,
						new BleAdvertisementData(token, psm).encode())
				.build();
		advertiser.startAdvertising(settings, data, advertiseCallback);
	}

	@GuardedBy("lock")
	private void startScanning(BluetoothAdapter adapter) {
		BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null) {
			LOG.warning("BLE scanning not supported");
			return;
		}
		ScanFilter filter = new ScanFilter.Builder()
				.setServiceData(SERVICE, new byte[0])
				.build();
		ScanSettings settings = new ScanSettings.Builder()
				.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
				.build();
		scanner.startScan(java.util.Collections.singletonList(filter),
				settings, scanCallback);
	}

	/**
	 * Restarts advertising at each epoch boundary so the token, and on most
	 * devices the random address, change together.
	 */
	@GuardedBy("lock")
	private void scheduleTokenRotation() {
		long now = clock.currentTimeMillis();
		long delay = EPOCH_MS - (now % EPOCH_MS) + 1000;
		handler.postDelayed(this::rotateToken, delay);
	}

	private void rotateToken() {
		synchronized (lock) {
			if (!up || serverSocket == null) return;
			BluetoothAdapter adapter = getAdapter();
			if (adapter == null) return;
			try {
				BluetoothLeAdvertiser advertiser =
						adapter.getBluetoothLeAdvertiser();
				if (advertiser != null) {
					advertiser.stopAdvertising(advertiseCallback);
				}
				startAdvertising(adapter, serverSocket.getPsm());
			} catch (IllegalStateException | SecurityException e) {
				logException(LOG, WARNING, e);
			}
			scheduleTokenRotation();
		}
	}

	@Nullable
	private BluetoothAdapter getAdapter() {
		BluetoothManager bm = (BluetoothManager)
				app.getSystemService(Context.BLUETOOTH_SERVICE);
		return bm == null ? null : bm.getAdapter();
	}

	private boolean hasPermissions() {
		if (SDK_INT >= 31) {
			return granted(BLUETOOTH_ADVERTISE) && granted(BLUETOOTH_SCAN)
					&& granted(BLUETOOTH_CONNECT);
		}
		// Android 10 and 11 require location access for BLE scans
		return granted(ACCESS_FINE_LOCATION);
	}

	private boolean granted(String permission) {
		return app.checkSelfPermission(permission) == PERMISSION_GRANTED;
	}

	private void setState(State newState) {
		if (state == newState) return;
		state = newState;
		callback.pluginStateChanged(newState);
	}

	private final BroadcastReceiver adapterReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent i) {
			if (i.getIntExtra(EXTRA_STATE, 0) == STATE_ON) {
				ioExecutor.execute(AndroidBlePlugin.this::bringUpIfPossible);
			} else {
				ioExecutor.execute(AndroidBlePlugin.this::tearDown);
			}
		}
	};

	private final AdvertiseCallback advertiseCallback =
			new AdvertiseCallback() {
				@Override
				public void onStartSuccess(AdvertiseSettings settings) {
					LOG.info("BLE advertising started");
				}

				@Override
				public void onStartFailure(int errorCode) {
					if (LOG.isLoggable(WARNING)) {
						LOG.warning("BLE advertising failed: " + errorCode);
					}
				}
			};

	private final ScanCallback scanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			ScanRecord record = result.getScanRecord();
			if (record == null) return;
			BleAdvertisementData data = BleAdvertisementData.decode(
					record.getServiceData(SERVICE));
			if (data == null) return;
			Sighting previous = sightings.put(toHexString(data.getToken()),
					new Sighting(result.getDevice(), data.getPsm(),
							clock.currentTimeMillis()));
			if (previous == null && LOG.isLoggable(INFO)) {
				LOG.info("Saw BLE token, PSM " + data.getPsm());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			if (LOG.isLoggable(WARNING)) {
				LOG.warning("BLE scan failed: " + errorCode);
			}
		}
	};

	private static class Sighting {

		private final BluetoothDevice device;
		private final int psm;
		private final long seen;

		private Sighting(BluetoothDevice device, int psm, long seen) {
			this.device = device;
			this.psm = psm;
			this.seen = seen;
		}
	}
}
