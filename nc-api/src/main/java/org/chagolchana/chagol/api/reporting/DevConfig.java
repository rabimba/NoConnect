package org.chagolchana.chagol.api.reporting;

import org.chagolchana.chagol.api.crypto.PublicKey;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.File;

@NotNullByDefault
public interface DevConfig {

	PublicKey getDevPublicKey();

	String getDevOnionAddress();

	File getReportDir();

	File getLogcatFile();
}
