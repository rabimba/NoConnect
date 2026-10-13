package org.chagolchana.noconnect.android.account;

import org.chagolchana.dontkillme.DozeHelper;
import org.chagolchana.dontkillme.DozeHelperImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class DozeHelperModule {

	@Provides
	DozeHelper provideDozeHelper() {
		return new DozeHelperImpl();
	}
}
