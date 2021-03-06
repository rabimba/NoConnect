package org.chagolchana.chagol.sync;

import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.chagol.api.sync.Ack;
import org.chagolchana.chagol.api.sync.MessageId;
import org.chagolchana.chagol.api.sync.Offer;
import org.chagolchana.chagol.api.sync.RecordTypes;
import org.chagolchana.chagol.api.sync.RecordWriter;
import org.chagolchana.chagol.api.sync.Request;
import org.chagolchana.chagol.util.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.concurrent.NotThreadSafe;

import static org.chagolchana.chagol.api.sync.RecordTypes.ACK;
import static org.chagolchana.chagol.api.sync.RecordTypes.OFFER;
import static org.chagolchana.chagol.api.sync.RecordTypes.REQUEST;
import static org.chagolchana.chagol.api.sync.SyncConstants.MAX_RECORD_PAYLOAD_LENGTH;
import static org.chagolchana.chagol.api.sync.SyncConstants.RECORD_HEADER_LENGTH;
import static org.chagolchana.chagol.api.sync.SyncConstants.PROTOCOL_VERSION;

@NotThreadSafe
@NotNullByDefault
class RecordWriterImpl implements RecordWriter {

	private final OutputStream out;
	private final byte[] header;
	private final ByteArrayOutputStream payload;

	RecordWriterImpl(OutputStream out) {
		this.out = out;
		header = new byte[RECORD_HEADER_LENGTH];
		header[0] = PROTOCOL_VERSION;
		payload = new ByteArrayOutputStream(MAX_RECORD_PAYLOAD_LENGTH);
	}

	private void writeRecord(byte recordType) throws IOException {
		header[1] = recordType;
		ByteUtils.writeUint16(payload.size(), header, 2);
		out.write(header);
		payload.writeTo(out);
		payload.reset();
	}

	@Override
	public void writeAck(Ack a) throws IOException {
		if (payload.size() != 0) throw new IllegalStateException();
		for (MessageId m : a.getMessageIds()) payload.write(m.getBytes());
		writeRecord(ACK);
	}

	@Override
	public void writeMessage(byte[] raw) throws IOException {
		header[1] = RecordTypes.MESSAGE;
		ByteUtils.writeUint16(raw.length, header, 2);
		out.write(header);
		out.write(raw);
	}

	@Override
	public void writeOffer(Offer o) throws IOException {
		if (payload.size() != 0) throw new IllegalStateException();
		for (MessageId m : o.getMessageIds()) payload.write(m.getBytes());
		writeRecord(OFFER);
	}

	@Override
	public void writeRequest(Request r) throws IOException {
		if (payload.size() != 0) throw new IllegalStateException();
		for (MessageId m : r.getMessageIds()) payload.write(m.getBytes());
		writeRecord(REQUEST);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}
}
