package org.chagolchana.chagol.api.reporting;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.File;

@NotNullByDefault
public interface DevConfig {

	File getLogcatFile();
}
