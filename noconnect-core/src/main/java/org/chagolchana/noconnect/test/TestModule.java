package org.chagolchana.noconnect.test;

import org.chagolchana.noconnect.api.test.TestDataCreator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TestModule {

	@Provides
	@Singleton
	TestDataCreator getTestDataCreator(TestDataCreatorImpl testDataCreator) {
		return testDataCreator;
	}

}
