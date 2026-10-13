package org.chagolchana.noconnect.android.conversation;

import org.chagolchana.noconnect.android.activity.ActivityScope;
import org.chagolchana.noconnect.android.conversation.glide.NoConnectDataFetcherFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ConversationModule {

	@ActivityScope
	@Provides
	NoConnectDataFetcherFactory provideNoConnectDataFetcherFactory(
			NoConnectDataFetcherFactory dataFetcherFactory) {
		return dataFetcherFactory;
	}

}
