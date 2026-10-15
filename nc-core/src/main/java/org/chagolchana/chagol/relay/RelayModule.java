package org.chagolchana.chagol.relay;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RelayModule {

	@Provides
	@Singleton
	RelayStore provideRelayStore() {
		return new MemoryRelayStore();
	}
}
