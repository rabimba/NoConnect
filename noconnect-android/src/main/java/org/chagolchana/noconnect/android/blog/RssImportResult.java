package org.chagolchana.noconnect.android.blog;

import org.chagolchana.nullsafety.NotNullByDefault;

@NotNullByDefault
abstract class RssImportResult {

	static class UrlImportSuccess extends RssImportResult {
	}

	static class UrlImportError extends RssImportResult {
		final String url;

		UrlImportError(String url) {
			this.url = url;
		}
	}

	static class FileImportSuccess extends RssImportResult {
	}

	static class FileImportError extends RssImportResult {
	}
}
