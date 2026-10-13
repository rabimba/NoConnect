package org.chagolchana.chagol.connection;

import org.chagolchana.chagol.api.connection.ConnectionManager;
import org.chagolchana.chagol.api.connection.ConnectionRegistry;
import org.chagolchana.chagol.api.contact.ContactExchangeManager;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.contact.HandshakeManager;
import org.chagolchana.chagol.api.contact.PendingContactId;
import org.chagolchana.chagol.api.lifecycle.IoExecutor;
import org.chagolchana.chagol.api.plugin.TransportConnectionReader;
import org.chagolchana.chagol.api.plugin.TransportConnectionWriter;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;
import org.chagolchana.chagol.api.properties.TransportPropertyManager;
import org.chagolchana.chagol.api.sync.OutgoingSessionRecord;
import org.chagolchana.chagol.api.sync.SyncSessionFactory;
import org.chagolchana.chagol.api.transport.KeyManager;
import org.chagolchana.chagol.api.transport.StreamReaderFactory;
import org.chagolchana.chagol.api.transport.StreamWriterFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.security.SecureRandom;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ConnectionManagerImpl implements ConnectionManager {

	private final Executor ioExecutor;
	private final KeyManager keyManager;
	private final StreamReaderFactory streamReaderFactory;
	private final StreamWriterFactory streamWriterFactory;
	private final SyncSessionFactory syncSessionFactory;
	private final HandshakeManager handshakeManager;
	private final ContactExchangeManager contactExchangeManager;
	private final ConnectionRegistry connectionRegistry;
	private final TransportPropertyManager transportPropertyManager;
	private final SecureRandom secureRandom;

	@Inject
	ConnectionManagerImpl(@IoExecutor Executor ioExecutor,
			KeyManager keyManager, StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			HandshakeManager handshakeManager,
			ContactExchangeManager contactExchangeManager,
			ConnectionRegistry connectionRegistry,
			TransportPropertyManager transportPropertyManager,
			SecureRandom secureRandom) {
		this.ioExecutor = ioExecutor;
		this.keyManager = keyManager;
		this.streamReaderFactory = streamReaderFactory;
		this.streamWriterFactory = streamWriterFactory;
		this.syncSessionFactory = syncSessionFactory;
		this.handshakeManager = handshakeManager;
		this.contactExchangeManager = contactExchangeManager;
		this.connectionRegistry = connectionRegistry;
		this.transportPropertyManager = transportPropertyManager;
		this.secureRandom = secureRandom;
	}


	@Override
	public void manageIncomingConnection(TransportId t,
			TransportConnectionReader r) {
		ioExecutor.execute(new IncomingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, t, r, null));
	}

	@Override
	public void manageIncomingConnection(TransportId t,
			TransportConnectionReader r, TagController c) {
		ioExecutor.execute(new IncomingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, t, r, c));
	}

	@Override
	public void manageIncomingConnection(TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new IncomingDuplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, ioExecutor,
				t, d));
	}

	@Override
	public void manageIncomingConnection(PendingContactId p, TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new IncomingHandshakeConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				handshakeManager, contactExchangeManager, this, p, t, d));
	}

	@Override
	public void manageOutgoingConnection(ContactId c, TransportId t,
			TransportConnectionWriter w) {
		ioExecutor.execute(new OutgoingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, c, t, w, null));
	}

	@Override
	public void manageOutgoingConnection(ContactId c, TransportId t,
			TransportConnectionWriter w, OutgoingSessionRecord sessionRecord) {
		ioExecutor.execute(new OutgoingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, c, t, w,
				sessionRecord));
	}

	@Override
	public void manageOutgoingConnection(ContactId c, TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new OutgoingDuplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, ioExecutor,
				secureRandom, c, t, d));
	}

	@Override
	public void manageOutgoingConnection(PendingContactId p, TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new OutgoingHandshakeConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				handshakeManager, contactExchangeManager, this, p, t, d));
	}
}
