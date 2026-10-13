package org.chagolchana.chagol.plugin.file;

import org.chagolchana.chagol.api.Pair;
import org.chagolchana.chagol.api.plugin.ConnectionHandler;
import org.chagolchana.chagol.api.plugin.PluginCallback;
import org.chagolchana.chagol.api.plugin.PluginException;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.util.Collection;

import static org.chagolchana.chagol.api.mailbox.MailboxConstants.ID;
import static org.chagolchana.chagol.api.plugin.Plugin.State.ACTIVE;

@NotNullByDefault
class MailboxPlugin extends FilePlugin {

	MailboxPlugin(PluginCallback callback, long maxLatency) {
		super(callback, maxLatency);
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public int getMaxIdleTime() {
		// Unused for simplex transports
		throw new UnsupportedOperationException();
	}

	@Override
	public void start() throws PluginException {
		callback.pluginStateChanged(ACTIVE);
	}

	@Override
	public void stop() throws PluginException {
	}

	@Override
	public State getState() {
		return ACTIVE;
	}

	@Override
	public int getReasonsDisabled() {
		return 0;
	}

	@Override
	public boolean shouldPoll() {
		return false;
	}

	@Override
	public int getPollingInterval() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void poll(
			Collection<Pair<TransportProperties, ConnectionHandler>> properties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLossyAndCheap() {
		return false;
	}
}
