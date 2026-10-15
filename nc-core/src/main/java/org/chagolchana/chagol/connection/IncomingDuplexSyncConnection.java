package org.chagolchana.chagol.connection;

import org.chagolchana.chagol.api.FeatureFlags;
import org.chagolchana.chagol.api.connection.ConnectionRegistry;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.chagol.api.properties.TransportPropertyManager;
import org.chagolchana.chagol.api.sync.PriorityHandler;
import org.chagolchana.chagol.api.sync.SyncSession;
import org.chagolchana.chagol.api.sync.SyncSessionFactory;
import org.chagolchana.chagol.api.transport.KeyManager;
import org.chagolchana.chagol.api.transport.StreamContext;
import org.chagolchana.chagol.api.transport.StreamReaderFactory;
import org.chagolchana.chagol.api.transport.StreamWriterFactory;
import org.chagolchana.chagol.relay.RelayStore;
import org.chagolchana.chagol.relay.StrangerSession;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.concurrent.Executor;

import static java.util.logging.Level.WARNING;
import static org.chagolchana.chagol.util.LogUtils.logException;

@NotNullByDefault
class IncomingDuplexSyncConnection extends DuplexSyncConnection
		implements Runnable {

	private final CryptoComponent crypto;
	private final RelayStore relayStore;
	private final FeatureFlags featureFlags;

	IncomingDuplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			Executor ioExecutor, CryptoComponent crypto, RelayStore relayStore,
			FeatureFlags featureFlags, TransportId transportId,
			DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager, ioExecutor, transportId, connection);
		this.crypto = crypto;
		this.relayStore = relayStore;
		this.featureFlags = featureFlags;
	}

	@Override
	public void run() {
		// Read and recognise the tag
		StreamContext ctx = recogniseTag(reader, transportId);
		if (ctx == null) {
			if (featureFlags.shouldEnableSealedEnvelopeRelay()) {
				LOG.info("Unrecognised tag, falling back to StrangerSession");
				try {
					new StrangerSession(crypto, relayStore, reader.getInputStream(),
							writer.getOutputStream(), false).run();
					reader.dispose(false, true);
					writer.dispose(false);
				} catch (IOException e) {
					onReadError(false);
				}
				return;
			}
			LOG.info("Unrecognised tag");
			onReadError(false);
			return;
		}
		ContactId contactId = ctx.getContactId();
		if (contactId == null) {
			LOG.warning("Expected contact tag, got rendezvous tag");
			onReadError(true);
			return;
		}
		if (ctx.isHandshakeMode()) {
			// TODO: Support handshake mode for contacts
			LOG.warning("Received handshake tag, expected rotation mode");
			onReadError(true);
			return;
		}
		connectionRegistry.registerIncomingConnection(contactId, transportId,
				this);
		// Start the outgoing session on another thread
		ioExecutor.execute(() -> runOutgoingSession(contactId));
		try {
			// Store any transport properties discovered from the connection
			transportPropertyManager.addRemotePropertiesFromConnection(
					contactId, transportId, remote);
			// Update the connection registry when we receive our priority
			PriorityHandler handler = p -> connectionRegistry.setPriority(
					contactId, transportId, this, p);
			// Create and run the incoming session
			createIncomingSession(ctx, reader, handler).run();
			reader.dispose(false, true);
			interruptOutgoingSession();
			connectionRegistry.unregisterConnection(contactId, transportId,
					this, true, false);
		} catch (DbException | IOException e) {
			logException(LOG, WARNING, e);
			onReadError(true);
			connectionRegistry.unregisterConnection(contactId, transportId,
					this, true, true);
		}
	}

	private void runOutgoingSession(ContactId contactId) {
		// Allocate a stream context
		StreamContext ctx = allocateStreamContext(contactId, transportId);
		if (ctx == null) {
			LOG.warning("Could not allocate stream context");
			onWriteError();
			return;
		}
		try {
			// Create and run the outgoing session
			SyncSession out = createDuplexOutgoingSession(ctx, writer, null);
			setOutgoingSession(out);
			out.run();
			writer.dispose(false);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			onWriteError();
		}
	}
}

