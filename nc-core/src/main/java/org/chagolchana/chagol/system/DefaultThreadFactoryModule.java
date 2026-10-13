package org.chagolchana.chagol.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DefaultThreadFactoryModule {
	@Provides
	@Singleton
	ThreadFactory provideThreadFactory() {
		return Executors.defaultThreadFactory();
	}
}
