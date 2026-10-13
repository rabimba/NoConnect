package org.chagolchana.chagol.record;

import org.chagolchana.chagol.api.record.RecordWriter;
import org.chagolchana.chagol.api.record.RecordWriterFactory;

import java.io.OutputStream;

class RecordWriterFactoryImpl implements RecordWriterFactory {

	@Override
	public RecordWriter createRecordWriter(OutputStream out) {
		return new RecordWriterImpl(out);
	}
}
