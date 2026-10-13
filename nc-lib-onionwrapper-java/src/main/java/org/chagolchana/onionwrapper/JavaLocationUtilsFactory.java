package org.chagolchana.onionwrapper;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public class JavaLocationUtilsFactory {

	public static LocationUtils createJavaLocationUtils() {
		return new JavaLocationUtils();
	}

}
