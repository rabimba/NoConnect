package org.chagolchana.chagol.api.sync;

import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.OutputStream;

@NotNullByDefault
public interface SyncRecordWriterFactory {

	SyncRecordWriter createRecordWriter(OutputStream out);
}
