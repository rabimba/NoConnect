package org.chagolchana.noconnect.android.logging;

import org.chagolchana.chagol.ChagolCoreModule;
import org.chagolchana.chagol.system.ClockModule;
import org.chagolchana.chagol.test.TestSecureRandomModule;

import java.security.SecureRandom;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		ClockModule.class,
		ChagolCoreModule.class,
		TestSecureRandomModule.class,
		LoggingModule.class,
		LoggingTestModule.class,
})
public interface LoggingComponent {

	SecureRandom random();

	CachingLogHandler cachingLogHandler();

	LogEncrypter logEncrypter();

	LogDecrypter logDecrypter();

}
