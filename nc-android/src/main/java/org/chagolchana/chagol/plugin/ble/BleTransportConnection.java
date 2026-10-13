package org.chagolchana.chagol.plugin.ble;

import android.bluetooth.BluetoothSocket;

import org.chagolchana.chagol.api.io.TimeoutMonitor;
import org.chagolchana.chagol.api.plugin.Plugin;
import org.chagolchana.chagol.api.plugin.duplex.AbstractDuplexTransportConnection;
import org.chagolchana.dontkillme.wakelock.AndroidWakeLock;
import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A duplex connection over a Bluetooth LE L2CAP channel.
 */
@NotNullByDefault
class BleTransportConnection extends AbstractDuplexTransportConnection {

	private final BluetoothSocket socket;
	private final InputStream in;
	private final AndroidWakeLock wakeLock;

	BleTransportConnection(Plugin plugin,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor, BluetoothSocket socket)
			throws IOException {
		super(plugin);
		this.socket = socket;
		InputStream socketIn = socket.getInputStream();
		if (socketIn == null) throw new IOException();
		in = timeoutMonitor.createTimeoutInputStream(socketIn,
				plugin.getMaxIdleTime() * 2L);
		wakeLock = wakeLockManager.createWakeLock("BleConnection");
		wakeLock.acquire();
	}

	@Override
	protected InputStream getInputStream() {
		return in;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		if (socketOut == null) throw new IOException();
		return socketOut;
	}

	@Override
	protected void closeConnection(boolean exception) throws IOException {
		try {
			socket.close();
			in.close();
		} finally {
			wakeLock.release();
		}
	}
}
