package org.chagolchana.chagol.sync;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.record.RecordReaderFactory;
import org.chagolchana.chagol.api.sync.SyncRecordReader;
import org.chagolchana.chagol.crypto.CryptoComponentImplFactory;
import org.chagolchana.chagol.record.RecordReaderFactoryImplFactory;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.util.Arrays;
import java.util.Random;

import static org.chagolchana.chagol.api.record.Record.RECORD_HEADER_BYTES;
import static org.chagolchana.chagol.api.sync.SyncConstants.PROTOCOL_VERSION;
import static org.chagolchana.chagol.util.ByteUtils.writeUint16;
import static org.junit.Assert.fail;

/**
 * Feeds random records to the record and sync record readers, which parse
 * data from untrusted peers. Reading must either succeed or throw
 * FormatException or EOFException: never another exception, a stack
 * overflow or an out-of-memory error. Seeded so failures reproduce.
 */
public class SyncRecordReaderRobustnessTest extends ChagolTestCase {

	private static final int ITERATIONS = 20_000;

	private final RecordReaderFactory recordReaderFactory =
			RecordReaderFactoryImplFactory.create();
	private final MessageFactoryImpl messageFactory =
			new MessageFactoryImpl(CryptoComponentImplFactory.create());

	@Test
	public void testRandomBytes() throws Exception {
		Random random = new Random(0x4e43_0003L);
		for (int i = 0; i < ITERATIONS; i++) {
			byte[] buf = new byte[random.nextInt(128)];
			random.nextBytes(buf);
			read(buf, i);
		}
	}

	@Test
	public void testWellFramedRecordsWithRandomPayloads() throws Exception {
		Random random = new Random(0x4e43_0004L);
		for (int i = 0; i < ITERATIONS; i++) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int records = 1 + random.nextInt(3);
			for (int r = 0; r < records; r++) {
				// Plausible lengths around ID and message boundaries
				int[] lengths = {0, 1, 15, 16, 17, 32, 40, 48, 64, 200};
				byte[] payload = new byte[lengths[random.nextInt(
						lengths.length)]];
				random.nextBytes(payload);
				byte[] header = new byte[RECORD_HEADER_BYTES];
				header[0] = random.nextInt(8) == 0
						? (byte) random.nextInt(256) : PROTOCOL_VERSION;
				header[1] = (byte) random.nextInt(7); // includes unknown type
				writeUint16(payload.length, header, 2);
				out.write(header);
				out.write(payload);
			}
			read(out.toByteArray(), i);
		}
	}

	private void read(byte[] buf, int iteration) {
		SyncRecordReader r = new SyncRecordReaderImpl(messageFactory,
				recordReaderFactory.createRecordReader(
						new ByteArrayInputStream(buf)));
		try {
			while (!r.eof()) {
				if (r.hasAck()) r.readAck();
				else if (r.hasMessage()) r.readMessage();
				else if (r.hasOffer()) r.readOffer();
				else if (r.hasRequest()) r.readRequest();
				else if (r.hasVersions()) r.readVersions();
				else if (r.hasPriority()) r.readPriority();
				else break;
			}
		} catch (FormatException | EOFException expected) {
			// Malformed input is rejected
		} catch (Throwable t) {
			fail("Iteration " + iteration + " input "
					+ Arrays.toString(buf) + " threw " + t);
		}
	}
}
