package org.chagolchana.onionwrapper;

import android.app.Application;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public class AndroidLocationUtilsFactory {

	public static LocationUtils createAndroidLocationUtils(Application app) {
		return new AndroidLocationUtils(app);
	}

}
