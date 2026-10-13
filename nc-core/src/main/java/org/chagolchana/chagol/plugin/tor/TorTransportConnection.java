package org.chagolchana.chagol.plugin.tor;

import org.chagolchana.chagol.api.plugin.Plugin;
import org.chagolchana.chagol.api.plugin.duplex.AbstractDuplexTransportConnection;
import org.chagolchana.chagol.util.IoUtils;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@NotNullByDefault
class TorTransportConnection extends AbstractDuplexTransportConnection {

	private final Socket socket;

	TorTransportConnection(Plugin plugin, Socket socket) {
		super(plugin);
		this.socket = socket;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return IoUtils.getInputStream(socket);
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return IoUtils.getOutputStream(socket);
	}

	@Override
	protected void closeConnection(boolean exception) throws IOException {
		socket.close();
	}
}
