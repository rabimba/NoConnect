package org.chagolchana.chagol.mailbox;

import org.chagolchana.nullsafety.NotNullByDefault;

import javax.inject.Inject;

@NotNullByDefault
class UrlConverterImpl implements UrlConverter {

	@Inject
	UrlConverterImpl() {
	}

	@Override
	public String convertOnionToBaseUrl(String onion) {
		return "http://" + onion + ".onion";
	}
}
