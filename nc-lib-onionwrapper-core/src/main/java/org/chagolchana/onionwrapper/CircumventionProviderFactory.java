package org.chagolchana.onionwrapper;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
public class CircumventionProviderFactory {

	public static CircumventionProvider createCircumventionProvider() {
		return new CircumventionProviderImpl();
	}

}
