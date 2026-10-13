package org.chagolchana.noconnect.android.controller;

import org.chagolchana.chagol.api.system.Wakeful;
import org.chagolchana.noconnect.android.controller.handler.ResultHandler;
import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface NoConnectController extends ActivityLifecycleController {

	void startAndBindService();

	boolean accountSignedIn();

	/**
	 * Returns true via the handler when the app has dozed
	 * without being white-listed.
	 */
	void hasDozed(ResultHandler<Boolean> handler);

	void doNotAskAgainForDozeWhiteListing();

	@Wakeful
	void signOut(ResultHandler<Void> handler, boolean deleteAccount);

	void deleteAccount();

}
