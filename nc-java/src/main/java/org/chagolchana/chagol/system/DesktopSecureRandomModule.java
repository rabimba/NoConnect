package org.chagolchana.chagol.system;

import org.chagolchana.chagol.api.system.SecureRandomProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.chagolchana.chagol.util.OsUtils.isLinux;
import static org.chagolchana.chagol.util.OsUtils.isMac;

@Module
public class DesktopSecureRandomModule {

	@Provides
	@Singleton
	SecureRandomProvider provideSecureRandomProvider() {
		if (isLinux()) return new UnixSecureRandomProvider();
		return () -> null; // Use system default
	}
}
