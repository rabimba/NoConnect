package org.chagolchana.chagol.connection;

import org.chagolchana.chagol.api.connection.ConnectionRegistry;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.plugin.TransportConnectionWriter;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.properties.TransportPropertyManager;
import org.chagolchana.chagol.api.sync.OutgoingSessionRecord;
import org.chagolchana.chagol.api.sync.SyncSession;
import org.chagolchana.chagol.api.sync.SyncSessionFactory;
import org.chagolchana.chagol.api.transport.KeyManager;
import org.chagolchana.chagol.api.transport.StreamContext;
import org.chagolchana.chagol.api.transport.StreamReaderFactory;
import org.chagolchana.chagol.api.transport.StreamWriter;
import org.chagolchana.chagol.api.transport.StreamWriterFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class OutgoingSimplexSyncConnection extends SyncConnection implements Runnable {

	private final ContactId contactId;
	private final TransportId transportId;
	private final TransportConnectionWriter writer;
	@Nullable
	private final OutgoingSessionRecord sessionRecord;

	OutgoingSimplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			ContactId contactId, TransportId transportId,
			TransportConnectionWriter writer,
			@Nullable OutgoingSessionRecord sessionRecord) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager);
		this.contactId = contactId;
		this.transportId = transportId;
		this.writer = writer;
		this.sessionRecord = sessionRecord;
	}

	@Override
	public void run() {
		// Allocate a stream context
		StreamContext ctx = allocateStreamContext(contactId, transportId);
		if (ctx == null) {
			LOG.warning("Could not allocate stream context");
			onError();
			return;
		}
		try {
			// Create and run the outgoing session
			createSimplexOutgoingSession(ctx, writer).run();
			writer.dispose(false);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			onError();
		}
	}

	private void onError() {
		disposeOnError(writer);
	}

	private SyncSession createSimplexOutgoingSession(StreamContext ctx,
			TransportConnectionWriter w) throws IOException {
		StreamWriter streamWriter = streamWriterFactory.createStreamWriter(
				w.getOutputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		if (sessionRecord == null) {
			// Use eager retransmission if the transport is lossy and cheap
			return syncSessionFactory.createSimplexOutgoingSession(c,
					ctx.getTransportId(), w.getMaxLatency(),
					w.isLossyAndCheap(), streamWriter);
		} else {
			return syncSessionFactory.createSimplexOutgoingSession(c,
					ctx.getTransportId(), w.getMaxLatency(), streamWriter,
					sessionRecord);
		}
	}
}

