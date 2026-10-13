package org.chagolchana.chagol.transport;

import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.crypto.StreamEncrypterFactory;
import org.chagolchana.chagol.api.transport.StreamContext;
import org.chagolchana.chagol.api.transport.StreamWriter;
import org.chagolchana.chagol.api.transport.StreamWriterFactory;
import org.chagolchana.nullsafety.NotNullByDefault;

import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class StreamWriterFactoryImpl implements StreamWriterFactory {

	private final StreamEncrypterFactory streamEncrypterFactory;

	@Inject
	StreamWriterFactoryImpl(StreamEncrypterFactory streamEncrypterFactory) {
		this.streamEncrypterFactory = streamEncrypterFactory;
	}

	@Override
	public StreamWriter createStreamWriter(OutputStream out,
			StreamContext ctx) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createStreamEncrypter(out, ctx));
	}

	@Override
	public StreamWriter createContactExchangeStreamWriter(OutputStream out,
			SecretKey headerKey) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createContactExchangeStreamEncrypter(out, headerKey));
	}

	@Override
	public StreamWriter createLogStreamWriter(OutputStream out,
			SecretKey headerKey) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createLogStreamEncrypter(out, headerKey));
	}
}
