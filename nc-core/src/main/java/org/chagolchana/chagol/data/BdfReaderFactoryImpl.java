package org.chagolchana.chagol.data;

import org.chagolchana.chagol.api.data.BdfReader;
import org.chagolchana.chagol.api.data.BdfReaderFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.annotation.concurrent.Immutable;

import static org.chagolchana.chagol.api.data.BdfReader.DEFAULT_MAX_BUFFER_SIZE;
import static org.chagolchana.chagol.api.data.BdfReader.DEFAULT_NESTED_LIMIT;

@Immutable
@NotNullByDefault
class BdfReaderFactoryImpl implements BdfReaderFactory {

	@Override
	public BdfReader createReader(InputStream in) {
		return new BdfReaderImpl(in, DEFAULT_NESTED_LIMIT,
				DEFAULT_MAX_BUFFER_SIZE, true);
	}

	@Override
	public BdfReader createReader(InputStream in, boolean canonical) {
		return new BdfReaderImpl(in, DEFAULT_NESTED_LIMIT,
				DEFAULT_MAX_BUFFER_SIZE, canonical);
	}

	@Override
	public BdfReader createReader(InputStream in, int nestedLimit,
			int maxBufferSize, boolean canonical) {
		return new BdfReaderImpl(in, nestedLimit, maxBufferSize, canonical);
	}
}
