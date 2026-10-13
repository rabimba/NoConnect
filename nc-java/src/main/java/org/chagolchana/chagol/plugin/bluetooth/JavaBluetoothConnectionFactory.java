package org.chagolchana.chagol.plugin.bluetooth;

import org.chagolchana.chagol.api.io.TimeoutMonitor;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPlugin;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;

import javax.annotation.concurrent.Immutable;
import javax.microedition.io.StreamConnection;

@Immutable
@NotNullByDefault
class JavaBluetoothConnectionFactory
		implements BluetoothConnectionFactory<StreamConnection> {

	private final BluetoothConnectionLimiter connectionLimiter;
	private final TimeoutMonitor timeoutMonitor;

	JavaBluetoothConnectionFactory(
			BluetoothConnectionLimiter connectionLimiter,
			TimeoutMonitor timeoutMonitor) {
		this.connectionLimiter = connectionLimiter;
		this.timeoutMonitor = timeoutMonitor;
	}

	@Override
	public DuplexTransportConnection wrapSocket(DuplexPlugin plugin,
			StreamConnection socket) throws IOException {
		return new JavaBluetoothTransportConnection(plugin, connectionLimiter,
				timeoutMonitor, socket);
	}
}
