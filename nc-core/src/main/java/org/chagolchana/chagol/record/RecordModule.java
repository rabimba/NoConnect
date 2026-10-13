package org.chagolchana.chagol.record;

import org.chagolchana.chagol.api.record.RecordReaderFactory;
import org.chagolchana.chagol.api.record.RecordWriterFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class RecordModule {

	@Provides
	RecordReaderFactory provideRecordReaderFactory() {
		return new RecordReaderFactoryImpl();
	}

	@Provides
	RecordWriterFactory provideRecordWriterFactory() {
		return new RecordWriterFactoryImpl();
	}
}
