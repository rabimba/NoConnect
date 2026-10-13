package org.chagolchana.chagol.connection;

import org.chagolchana.chagol.api.connection.ConnectionRegistry;
import org.chagolchana.chagol.api.contact.ContactId;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.plugin.TransportConnectionReader;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.properties.TransportPropertyManager;
import org.chagolchana.chagol.api.sync.PriorityHandler;
import org.chagolchana.chagol.api.sync.SyncSession;
import org.chagolchana.chagol.api.sync.SyncSessionFactory;
import org.chagolchana.chagol.api.transport.KeyManager;
import org.chagolchana.chagol.api.transport.StreamContext;
import org.chagolchana.chagol.api.transport.StreamReaderFactory;
import org.chagolchana.chagol.api.transport.StreamWriterFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static org.chagolchana.chagol.util.LogUtils.logException;
import static org.chagolchana.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class SyncConnection extends Connection {

	final SyncSessionFactory syncSessionFactory;
	final TransportPropertyManager transportPropertyManager;

	SyncConnection(KeyManager keyManager, ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory);
		this.syncSessionFactory = syncSessionFactory;
		this.transportPropertyManager = transportPropertyManager;
	}

	@Nullable
	StreamContext allocateStreamContext(ContactId contactId,
			TransportId transportId) {
		try {
			return keyManager.getStreamContext(contactId, transportId);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	SyncSession createIncomingSession(StreamContext ctx,
			TransportConnectionReader r, PriorityHandler handler)
			throws IOException {
		InputStream streamReader = streamReaderFactory.createStreamReader(
				r.getInputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		return syncSessionFactory
				.createIncomingSession(c, streamReader, handler);
	}
}
