package org.chagolchana.chagol.connection;

import org.chagolchana.chagol.api.connection.ConnectionRegistry;
import org.chagolchana.chagol.api.connection.InterruptibleConnection;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.plugin.TransportConnectionReader;
import org.chagolchana.chagol.api.plugin.TransportConnectionWriter;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.chagol.api.properties.TransportPropertyManager;
import org.chagolchana.chagol.api.sync.Priority;
import org.chagolchana.chagol.api.sync.SyncSession;
import org.chagolchana.chagol.api.sync.SyncSessionFactory;
import org.chagolchana.chagol.api.transport.KeyManager;
import org.chagolchana.chagol.api.transport.StreamContext;
import org.chagolchana.chagol.api.transport.StreamReaderFactory;
import org.chagolchana.chagol.api.transport.StreamWriter;
import org.chagolchana.chagol.api.transport.StreamWriterFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static org.chagolchana.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
abstract class DuplexSyncConnection extends SyncConnection
		implements InterruptibleConnection {

	final Executor ioExecutor;
	final TransportId transportId;
	final TransportConnectionReader reader;
	final TransportConnectionWriter writer;
	final TransportProperties remote;

	private final Object interruptLock = new Object();

	@GuardedBy("interruptLock")
	@Nullable
	private SyncSession outgoingSession = null;
	@GuardedBy("interruptLock")
	private boolean interruptWaiting = false;

	@Override
	public void interruptOutgoingSession() {
		SyncSession out = null;
		synchronized (interruptLock) {
			if (outgoingSession == null) interruptWaiting = true;
			else out = outgoingSession;
		}
		if (out != null) out.interrupt();
	}

	void setOutgoingSession(SyncSession outgoingSession) {
		boolean interruptWasWaiting = false;
		synchronized (interruptLock) {
			this.outgoingSession = outgoingSession;
			if (interruptWaiting) {
				interruptWasWaiting = true;
				interruptWaiting = false;
			}
		}
		if (interruptWasWaiting) outgoingSession.interrupt();
	}

	DuplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			Executor ioExecutor, TransportId transportId,
			DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager);
		this.ioExecutor = ioExecutor;
		this.transportId = transportId;
		reader = connection.getReader();
		writer = connection.getWriter();
		remote = connection.getRemoteProperties();
	}

	void onReadError(boolean recognised) {
		disposeOnError(reader, recognised);
		disposeOnError(writer);
		interruptOutgoingSession();
	}

	void onWriteError() {
		disposeOnError(reader, true);
		disposeOnError(writer);
	}

	SyncSession createDuplexOutgoingSession(StreamContext ctx,
			TransportConnectionWriter w, @Nullable Priority priority)
			throws IOException {
		StreamWriter streamWriter = streamWriterFactory.createStreamWriter(
				w.getOutputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		return syncSessionFactory.createDuplexOutgoingSession(c,
				ctx.getTransportId(), w.getMaxLatency(), w.getMaxIdleTime(),
				streamWriter, priority);
	}
}
