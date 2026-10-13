package org.chagolchana.chagol.record;

import org.chagolchana.chagol.api.record.RecordReader;
import org.chagolchana.chagol.api.record.RecordReaderFactory;

import java.io.InputStream;

class RecordReaderFactoryImpl implements RecordReaderFactory {

	@Override
	public RecordReader createRecordReader(InputStream in) {
		return new RecordReaderImpl(in);
	}
}
