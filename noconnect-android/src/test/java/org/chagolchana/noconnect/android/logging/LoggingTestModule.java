package org.chagolchana.noconnect.android.logging;

import org.chagolchana.chagol.api.reporting.DevConfig;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class LoggingTestModule {

	private final File logFile;

	LoggingTestModule(File logFile) {
		this.logFile = logFile;
	}

	@Provides
	@Singleton
	DevConfig provideDevConfig() {
		@NotNullByDefault
		DevConfig devConfig = new DevConfig() {
			@Override
			public File getLogcatFile() {
				return logFile;
			}
		};
		return devConfig;
	}

}
