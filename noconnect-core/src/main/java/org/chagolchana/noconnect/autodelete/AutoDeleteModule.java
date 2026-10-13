package org.chagolchana.noconnect.autodelete;

import org.chagolchana.chagol.api.contact.ContactManager;
import org.chagolchana.chagol.api.lifecycle.LifecycleManager;
import org.chagolchana.noconnect.api.autodelete.AutoDeleteManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AutoDeleteModule {

	public static class EagerSingletons {
		@Inject
		AutoDeleteManager autoDeleteManager;
	}

	@Provides
	@Singleton
	AutoDeleteManager provideAutoDeleteManager(
			LifecycleManager lifecycleManager, ContactManager contactManager,
			AutoDeleteManagerImpl autoDeleteManager) {
		lifecycleManager.registerOpenDatabaseHook(autoDeleteManager);
		contactManager.registerContactHook(autoDeleteManager);
		// Don't need to register with the client versioning manager as this
		// client's groups aren't shared with contacts
		return autoDeleteManager;
	}
}
