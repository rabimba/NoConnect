package org.chagolchana.chagol.invitation;

import org.chagolchana.chagol.api.contact.ContactExchangeTask;
import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.PseudoRandom;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.data.BdfReader;
import org.chagolchana.chagol.api.data.BdfReaderFactory;
import org.chagolchana.chagol.api.data.BdfWriter;
import org.chagolchana.chagol.api.data.BdfWriterFactory;
import org.chagolchana.chagol.api.identity.LocalAuthor;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.plugin.duplex.DuplexPlugin;
import org.chagolchana.chagol.api.plugin.duplex.DuplexTransportConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * A connection thread for the peer being Bob in the invitation protocol.
 */
@NotNullByDefault
class BobConnector extends Connector {

	private static final Logger LOG =
			Logger.getLogger(BobConnector.class.getName());

	BobConnector(CryptoComponent crypto, BdfReaderFactory bdfReaderFactory,
			BdfWriterFactory bdfWriterFactory,
			ContactExchangeTask contactExchangeTask, ConnectorGroup group,
			DuplexPlugin plugin, LocalAuthor localAuthor, PseudoRandom random) {
		super(crypto, bdfReaderFactory, bdfWriterFactory, contactExchangeTask,
				group, plugin, localAuthor, random);
	}

	@Override
	public void run() {
		// Create an incoming or outgoing connection
		DuplexTransportConnection conn = createInvitationConnection(false);
		if (conn == null) return;
		if (LOG.isLoggable(INFO)) LOG.info(pluginName + " connected");
		// Carry out the key agreement protocol
		InputStream in;
		OutputStream out;
		BdfReader r;
		BdfWriter w;
		SecretKey master;
		try {
			in = conn.getReader().getInputStream();
			out = conn.getWriter().getOutputStream();
			r = bdfReaderFactory.createReader(in);
			w = bdfWriterFactory.createWriter(out);
			// Alice goes first
			byte[] hash = receivePublicKeyHash(r);
			// Don't proceed with more than one connection
			if (group.getAndSetConnected()) {
				if (LOG.isLoggable(INFO)) LOG.info(pluginName + " redundant");
				tryToClose(conn, false);
				return;
			}
			sendPublicKeyHash(w);
			byte[] key = receivePublicKey(r);
			sendPublicKey(w);
			master = deriveMasterSecret(hash, key, false);
		} catch (IOException e) {
			if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
			group.keyAgreementFailed();
			tryToClose(conn, true);
			return;
		} catch (GeneralSecurityException e) {
			if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
			group.keyAgreementFailed();
			tryToClose(conn, true);
			return;
		}
		// The key agreement succeeded - derive the confirmation codes
		if (LOG.isLoggable(INFO)) LOG.info(pluginName + " agreement succeeded");
		int aliceCode = crypto.deriveBTConfirmationCode(master, true);
		int bobCode = crypto.deriveBTConfirmationCode(master, false);
		group.keyAgreementSucceeded(bobCode, aliceCode);
		// Exchange confirmation results
		boolean localMatched, remoteMatched;
		try {
			remoteMatched = receiveConfirmation(r);
			localMatched = group.waitForLocalConfirmationResult();
			sendConfirmation(w, localMatched);
		} catch (IOException e) {
			if (LOG.isLoggable(WARNING)) LOG.log(WARNING, e.toString(), e);
			group.remoteConfirmationFailed();
			tryToClose(conn, true);
			return;
		} catch (InterruptedException e) {
			LOG.warning("Interrupted while waiting for confirmation");
			group.remoteConfirmationFailed();
			tryToClose(conn, true);
			Thread.currentThread().interrupt();
			return;
		}
		if (remoteMatched) group.remoteConfirmationSucceeded();
		else group.remoteConfirmationFailed();
		if (!(localMatched && remoteMatched)) {
			if (LOG.isLoggable(INFO))
				LOG.info(pluginName + " confirmation failed");
			tryToClose(conn, false);
			return;
		}
		// Confirmation succeeded - upgrade to a secure connection
		if (LOG.isLoggable(INFO))
			LOG.info(pluginName + " confirmation succeeded");
		contactExchangeTask.startExchange(group, localAuthor, master, conn,
				plugin.getId(), false);
	}
}
