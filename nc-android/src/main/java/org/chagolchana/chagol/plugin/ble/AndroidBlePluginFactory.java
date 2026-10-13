package org.chagolchana.chagol.plugin.ble;

import android.app.Application;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.io.TimeoutMonitor;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.plugin.PluginCallback;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPlugin;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPluginFactory;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.security.SecureRandom;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.chagolchana.chagol.api.plugin.BleConstants.ID;

@Immutable
@NotNullByDefault
public class AndroidBlePluginFactory implements DuplexPluginFactory {

	private static final int MAX_LATENCY = 30 * 1000; // 30 seconds
	private static final int MAX_IDLE_TIME = 30 * 1000; // 30 seconds

	private final Application app;
	private final Executor ioExecutor;
	private final AndroidWakeLockManager wakeLockManager;
	private final TimeoutMonitor timeoutMonitor;
	private final SecureRandom secureRandom;
	private final Clock clock;
	private final CryptoComponent crypto;
	private final EventBus eventBus;

	@Inject
	AndroidBlePluginFactory(Application app, @IoExecutor Executor ioExecutor,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor, SecureRandom secureRandom,
			Clock clock, CryptoComponent crypto, EventBus eventBus) {
		this.app = app;
		this.ioExecutor = ioExecutor;
		this.wakeLockManager = wakeLockManager;
		this.timeoutMonitor = timeoutMonitor;
		this.secureRandom = secureRandom;
		this.clock = clock;
		this.crypto = crypto;
		this.eventBus = eventBus;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return MAX_LATENCY;
	}

	@Override
	public DuplexPlugin createPlugin(PluginCallback callback) {
		return new AndroidBlePlugin(app, ioExecutor, wakeLockManager,
				timeoutMonitor, secureRandom, clock, crypto, eventBus, callback,
				MAX_LATENCY, MAX_IDLE_TIME);
	}
}
