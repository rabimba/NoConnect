package org.chagolchana.noconnect.android;

import org.chagolchana.chagol.ChagolAndroidModule;
import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.account.NoConnectAccountModule;
import org.chagolchana.chagol.mailbox.ModularMailboxModule;
import org.chagolchana.chagol.plugin.file.RemovableDriveModule;
import org.chagolchana.chagol.system.ClockModule;
import org.chagolchana.noconnect.NoConnectCoreModule;
import org.chagolchana.noconnect.android.account.SignInTestCreateAccount;
import org.chagolchana.noconnect.android.account.SignInTestSignIn;
import org.chagolchana.noconnect.android.attachment.AttachmentModule;
import org.chagolchana.noconnect.android.attachment.media.MediaModule;
import org.chagolchana.noconnect.android.navdrawer.NavDrawerActivityTest;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		AppModule.class,
		AttachmentModule.class,
		ClockModule.class,
		MediaModule.class,
		RemovableDriveModule.class,
		NoConnectCoreModule.class,
		ChagolAndroidModule.class,
		NoConnectAccountModule.class,
		ChagolCoreModule.class,
		ModularMailboxModule.class
})
public interface NoConnectUiTestComponent extends AndroidComponent {

	void inject(NavDrawerActivityTest test);

	void inject(SignInTestCreateAccount test);

	void inject(SignInTestSignIn test);

}
