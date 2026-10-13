package org.chagolchana.chagol.account;

import org.chagolchana.chagol.api.account.AccountManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NoConnectAccountModule {

	@Provides
	@Singleton
	AccountManager provideAccountManager(NoConnectAccountManager accountManager) {
		return accountManager;
	}
}
