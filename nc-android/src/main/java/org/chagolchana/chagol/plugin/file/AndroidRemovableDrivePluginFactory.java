package org.chagolchana.chagol.plugin.file;

import android.app.Application;

import org.chagolchana.chagol.api.plugin.PluginCallback;
import org.chagolchana.chagol.api.plugin.TransportId;
import org.chagolchana.chagol.api.plugin.simplex.SimplexPlugin;
import org.chagolchana.chagol.api.plugin.simplex.SimplexPluginFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.chagolchana.chagol.api.plugin.file.RemovableDriveConstants.ID;

@Immutable
@NotNullByDefault
public class AndroidRemovableDrivePluginFactory implements
		SimplexPluginFactory {

	private static final long MAX_LATENCY = DAYS.toMillis(28);

	private final Application app;

	@Inject
	AndroidRemovableDrivePluginFactory(Application app) {
		this.app = app;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return MAX_LATENCY;
	}

	@Nullable
	@Override
	public SimplexPlugin createPlugin(PluginCallback callback) {
		return new AndroidRemovableDrivePlugin(app, callback, MAX_LATENCY);
	}
}
