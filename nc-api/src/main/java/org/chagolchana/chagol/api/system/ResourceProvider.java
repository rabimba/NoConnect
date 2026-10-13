package org.chagolchana.chagol.api.system;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.InputStream;

@NotNullByDefault
public interface ResourceProvider {

	InputStream getResourceInputStream(String name, String extension);
}
