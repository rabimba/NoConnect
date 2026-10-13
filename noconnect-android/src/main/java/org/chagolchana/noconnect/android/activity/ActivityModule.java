package org.chagolchana.noconnect.android.activity;

import android.app.Activity;

import org.chagolchana.noconnect.android.controller.NoConnectController;
import org.chagolchana.noconnect.android.controller.NoConnectControllerImpl;
import org.chagolchana.noconnect.android.controller.DbController;
import org.chagolchana.noconnect.android.controller.DbControllerImpl;

import dagger.Module;
import dagger.Provides;

import static org.chagolchana.noconnect.android.NoConnectService.NoConnectServiceConnection;

@Module
public class ActivityModule {

	private final BaseActivity activity;

	public ActivityModule(BaseActivity activity) {
		this.activity = activity;
	}

	@ActivityScope
	@Provides
	BaseActivity provideBaseActivity() {
		return activity;
	}

	@ActivityScope
	@Provides
	Activity provideActivity() {
		return activity;
	}

	@ActivityScope
	@Provides
	protected NoConnectController provideNoConnectController(
			NoConnectControllerImpl noConnectController) {
		activity.addLifecycleController(noConnectController);
		return noConnectController;
	}

	@ActivityScope
	@Provides
	DbController provideDBController(DbControllerImpl dbController) {
		return dbController;
	}

	@ActivityScope
	@Provides
	NoConnectServiceConnection provideNoConnectServiceConnection() {
		return new NoConnectServiceConnection();
	}
}
