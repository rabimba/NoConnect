package org.chagolchana.chagol.plugin.tor;

import android.app.Application;

import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.chagol.api.battery.BatteryManager;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.event.EventBus;
import org.chagolchana.chagol.api.event.EventExecutor;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.network.NetworkManager;
import org.chagolchana.chagol.api.plugin.Backoff;
import org.chagolchana.chagol.api.plugin.BackoffFactory;
import org.chagolchana.chagol.api.plugin.PluginCallback;
import org.chagolchana.chagol.api.plugin.TorControlPort;
import org.chagolchana.chagol.api.plugin.TorDirectory;
import org.chagolchana.chagol.api.plugin.TorSocksPort;
import org.chagolchana.chagol.api.system.Clock;
import org.chagolchana.chagol.api.system.WakefulIoExecutor;
import org.chagolchana.nullsafety.NotNullByDefault;
import org.chagolchana.onionwrapper.AndroidTorWrapper;
import org.chagolchana.onionwrapper.CircumventionProvider;
import org.chagolchana.onionwrapper.LocationUtils;
import org.chagolchana.onionwrapper.TorWrapper;

import java.io.File;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.net.SocketFactory;

import static org.chagolchana.chagol.util.AndroidUtils.getSupportedArchitectures;

@Immutable
@NotNullByDefault
public class AndroidTorPluginFactory extends TorPluginFactory {

	private final Application app;
	private final AndroidWakeLockManager wakeLockManager;

	@Inject
	AndroidTorPluginFactory(@IoExecutor Executor ioExecutor,
			@EventExecutor Executor eventExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			NetworkManager networkManager,
			LocationUtils locationUtils,
			EventBus eventBus,
			SocketFactory torSocketFactory,
			BackoffFactory backoffFactory,
			CircumventionProvider circumventionProvider,
			BatteryManager batteryManager,
			Clock clock,
			CryptoComponent crypto,
			@TorDirectory File torDirectory,
			@TorSocksPort int torSocksPort,
			@TorControlPort int torControlPort,
			Application app,
			AndroidWakeLockManager wakeLockManager) {
		super(ioExecutor, eventExecutor, wakefulIoExecutor, networkManager,
				locationUtils, eventBus, torSocketFactory, backoffFactory,
				circumventionProvider, batteryManager, clock, crypto,
				torDirectory, torSocksPort, torControlPort);
		this.app = app;
		this.wakeLockManager = wakeLockManager;
	}

	@Nullable
	@Override
	String getArchitectureForTorBinary() {
		for (String abi : getSupportedArchitectures()) {
			if (abi.startsWith("x86_64")) return "x86_64_pie";
			else if (abi.startsWith("x86")) return "x86_pie";
			else if (abi.startsWith("arm64")) return "arm64_pie";
			else if (abi.startsWith("armeabi")) return "arm_pie";
		}
		return null;
	}

	@Override
	TorPlugin createPluginInstance(Backoff backoff,
			TorRendezvousCrypto torRendezvousCrypto, PluginCallback callback,
			String architecture) {
		TorWrapper tor = new AndroidTorWrapper(app, wakeLockManager,
				ioExecutor, eventExecutor, architecture, torDirectory,
				torSocksPort, torControlPort);
		return new TorPlugin(ioExecutor, wakefulIoExecutor,
				networkManager, locationUtils, torSocketFactory,
				circumventionProvider, batteryManager, backoff,
				torRendezvousCrypto, tor, callback, MAX_LATENCY,
				MAX_IDLE_TIME);
	}
}
