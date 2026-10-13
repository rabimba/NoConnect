package org.chagolchana.chagol.plugin.tor;

import org.chagolchana.onionwrapper.CircumventionProvider;
import org.chagolchana.onionwrapper.CircumventionProviderFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CircumventionModule {

	@Provides
	@Singleton
	CircumventionProvider provideCircumventionProvider() {
		return CircumventionProviderFactory.createCircumventionProvider();
	}
}
