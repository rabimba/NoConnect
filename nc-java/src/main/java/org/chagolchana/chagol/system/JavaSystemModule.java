package org.chagolchana.chagol.system;

import org.chagolchana.chagol.api.system.ResourceProvider;
import org.chagolchana.onionwrapper.JavaLocationUtilsFactory;
import org.chagolchana.onionwrapper.LocationUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class JavaSystemModule {

	@Provides
	@Singleton
	LocationUtils provideLocationUtils() {
		return JavaLocationUtilsFactory.createJavaLocationUtils();
	}

	@Provides
	@Singleton
	ResourceProvider provideResourceProvider(JavaResourceProvider provider) {
		return provider;
	}
}
