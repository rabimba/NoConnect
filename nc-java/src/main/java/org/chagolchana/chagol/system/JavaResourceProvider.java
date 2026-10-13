package org.chagolchana.chagol.system;

import org.chagolchana.chagol.api.system.ResourceProvider;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.inject.Inject;

import static org.chagolchana.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class JavaResourceProvider implements ResourceProvider {

	@Inject
	JavaResourceProvider() {
	}

	@Override
	public InputStream getResourceInputStream(String name, String extension) {
		ClassLoader cl = getClass().getClassLoader();
		return requireNonNull(cl.getResourceAsStream(name + extension));
	}
}
