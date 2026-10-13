package org.chagolchana.noconnect.client;

import org.chagolchana.noconnect.api.client.MessageTracker;

import dagger.Module;
import dagger.Provides;

@Module
public class NoConnectClientModule {

	@Provides
	MessageTracker provideMessageTracker(MessageTrackerImpl messageTracker) {
		return messageTracker;
	}
}
