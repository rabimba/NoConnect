package org.chagolchana.chagol.plugin.file;

import org.chagolchana.chagol.api.Pair;
import org.chagolchana.chagol.api.plugin.ConnectionHandler;
import org.chagolchana.chagol.api.plugin.PluginCallback;
import org.chagolchana.chagol.api.plugin.TransportConnectionReader;
import org.chagolchana.chagol.api.plugin.TransportConnectionWriter;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.simplex.SimplexPlugin;
import org.chagolchana.chagol.api.properties.TransportProperties;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.singletonMap;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.chagolchana.chagol.api.plugin.Plugin.State.ACTIVE;
import static org.chagolchana.chagol.api.plugin.file.RemovableDriveConstants.ID;
import static org.chagolchana.chagol.api.plugin.file.RemovableDriveConstants.PROP_SUPPORTED;
import static org.chagolchana.chagol.util.LogUtils.logException;

@Immutable
@NotNullByDefault
abstract class AbstractRemovableDrivePlugin implements SimplexPlugin {

	private static final Logger LOG =
			getLogger(AbstractRemovableDrivePlugin.class.getName());

	private final long maxLatency;
	private final PluginCallback callback;

	abstract InputStream openInputStream(TransportProperties p)
			throws IOException;

	abstract OutputStream openOutputStream(TransportProperties p)
			throws IOException;

	AbstractRemovableDrivePlugin(PluginCallback callback, long maxLatency) {
		this.callback = callback;
		this.maxLatency = maxLatency;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return maxLatency;
	}

	@Override
	public int getMaxIdleTime() {
		// Unused for simplex transports
		throw new UnsupportedOperationException();
	}

	@Override
	public void start() {
		callback.mergeLocalProperties(
				new TransportProperties(singletonMap(PROP_SUPPORTED, "true")));
		callback.pluginStateChanged(ACTIVE);
	}

	@Override
	public void stop() {
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
		return true;
	}

	@Override
	public TransportConnectionReader createReader(TransportProperties p) {
		try {
			return new TransportInputStreamReader(openInputStream(p));
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	@Override
	public TransportConnectionWriter createWriter(TransportProperties p) {
		try {
			return new TransportOutputStreamWriter(this, openOutputStream(p));
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}
}
