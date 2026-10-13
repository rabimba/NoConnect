package org.chagolchana.chagol.plugin.bluetooth;

import android.bluetooth.BluetoothSocket;

import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.chagol.api.io.TimeoutMonitor;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPlugin;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
class AndroidBluetoothConnectionFactory
		implements BluetoothConnectionFactory<BluetoothSocket> {

	private final BluetoothConnectionLimiter connectionLimiter;
	private final AndroidWakeLockManager wakeLockManager;
	private final TimeoutMonitor timeoutMonitor;

	AndroidBluetoothConnectionFactory(
			BluetoothConnectionLimiter connectionLimiter,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor) {
		this.connectionLimiter = connectionLimiter;
		this.wakeLockManager = wakeLockManager;
		this.timeoutMonitor = timeoutMonitor;
	}

	@Override
	public DuplexTransportConnection wrapSocket(DuplexPlugin plugin,
			BluetoothSocket s) throws IOException {
		return new AndroidBluetoothTransportConnection(plugin,
				connectionLimiter, wakeLockManager, timeoutMonitor, s);
	}
}
