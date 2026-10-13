package org.chagolchana.noconnect.android;

import org.chagolchana.chagol.ChagolAndroidEagerSingletons;
import org.chagolchana.chagol.ChagolCoreEagerSingletons;
import org.chagolchana.noconnect.NoConnectCoreEagerSingletons;

public class NoConnectTestComponentApplication extends NoConnectApplicationImpl {

	@Override
	protected AndroidComponent createApplicationComponent() {
		AndroidComponent component = DaggerNoConnectUiTestComponent.builder()
				.appModule(new AppModule(this)).build();
		// We need to load the eager singletons directly after making the
		// dependency graphs
		ChagolCoreEagerSingletons.Helper.injectEagerSingletons(component);
		ChagolAndroidEagerSingletons.Helper.injectEagerSingletons(component);
		NoConnectCoreEagerSingletons.Helper.injectEagerSingletons(component);
		AndroidEagerSingletons.Helper.injectEagerSingletons(component);
		return component;
	}

	@Override
	public boolean isInstrumentationTest() {
		return true;
	}

}
