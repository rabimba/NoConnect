package org.chagolchana.chagol.test;

import org.chagolchana.chagol.api.mailbox.MailboxDirectory;

import java.io.File;

import dagger.Module;
import dagger.Provides;

@Module
public class TestMailboxDirectoryModule {

	@Provides
	@MailboxDirectory
	File provideMailboxDirectory() {
		return new File("mailbox");
	}
}
