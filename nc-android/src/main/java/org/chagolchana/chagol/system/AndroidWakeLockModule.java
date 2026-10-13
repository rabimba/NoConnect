package org.chagolchana.chagol.system;

import android.app.Application;

import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManager;
import org.chagolchana.dontkillme.wakelock.AndroidWakeLockManagerFactory;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidWakeLockModule {

	@Provides
	@Singleton
	AndroidWakeLockManager provideWakeLockManager(Application app,
			ScheduledExecutorService scheduledExecutorService) {
		return AndroidWakeLockManagerFactory.createAndroidWakeLockManager(app,
				scheduledExecutorService);
	}
}
