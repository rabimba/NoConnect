package org.chagolchana.chagol.plugin.bluetooth;

import android.bluetooth.BluetoothSocket;

import org.chagolchana.dontkillme.wakelock.AndroidWakeLock;
import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.chagol.api.io.TimeoutMonitor;
import org.chagolchana.chagol.api.plugin.Plugin;
import org.chagolchana.chagol.api.plugin.duplex.AbstractDuplexTransportConnection;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.chagolchana.chagol.api.plugin.BluetoothConstants.PROP_ADDRESS;
import static org.chagolchana.chagol.util.AndroidUtils.isValidBluetoothAddress;

@NotNullByDefault
class AndroidBluetoothTransportConnection
		extends AbstractDuplexTransportConnection {

	private final BluetoothConnectionLimiter connectionLimiter;
	private final BluetoothSocket socket;
	private final InputStream in;
	private final AndroidWakeLock wakeLock;

	AndroidBluetoothTransportConnection(Plugin plugin,
			BluetoothConnectionLimiter connectionLimiter,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor,
			BluetoothSocket socket) throws IOException {
		super(plugin);
		this.connectionLimiter = connectionLimiter;
		this.socket = socket;
		InputStream socketIn = socket.getInputStream();
		if (socketIn == null) throw new IOException();
		in = timeoutMonitor.createTimeoutInputStream(socketIn,
				plugin.getMaxIdleTime() * 2L);
		wakeLock = wakeLockManager.createWakeLock("BluetoothConnection");
		wakeLock.acquire();
		String address = socket.getRemoteDevice().getAddress();
		if (isValidBluetoothAddress(address)) remote.put(PROP_ADDRESS, address);
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
			connectionLimiter.connectionClosed(this);
		}
	}
}
