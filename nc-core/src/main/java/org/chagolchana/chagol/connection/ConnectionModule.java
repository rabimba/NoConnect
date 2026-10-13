package org.chagolchana.chagol.connection;

import org.chagolchana.chagol.api.connection.ConnectionManager;
import org.chagolchana.chagol.api.connection.ConnectionRegistry;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ConnectionModule {

	@Provides
	ConnectionManager provideConnectionManager(
			ConnectionManagerImpl connectionManager) {
		return connectionManager;
	}

	@Provides
	@Singleton
	ConnectionRegistry provideConnectionRegistry(
			ConnectionRegistryImpl connectionRegistry) {
		return connectionRegistry;
	}
}
