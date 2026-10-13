package org.chagolchana.chagol.record;

import org.chagolchana.chagol.api.record.RecordReaderFactory;

/**
 * Gives tests in other packages access to the package-private
 * {@link RecordReaderFactoryImpl}.
 */
public class RecordReaderFactoryImplFactory {

	public static RecordReaderFactory create() {
		return new RecordReaderFactoryImpl();
	}
}
