package org.chagolchana.chagol.data;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.test.ChagolTestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.chagolchana.chagol.api.data.BdfReader.DEFAULT_MAX_BUFFER_SIZE;
import static org.chagolchana.chagol.api.data.BdfReader.DEFAULT_NESTED_LIMIT;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Feeds random and mutated input to the BDF reader, which parses data from
 * untrusted peers. The reader must either succeed or throw FormatException or
 * EOFException: never another exception, a stack overflow or an out-of-memory
 * error. Seeded so failures reproduce.
 */
public class BdfReaderRobustnessTest extends ChagolTestCase {

	private static final int ITERATIONS = 20_000;

	@Test
	public void testRandomInput() throws Exception {
		Random random = new Random(0x4e43_0001L);
		for (int i = 0; i < ITERATIONS; i++) {
			byte[] buf = new byte[1 + random.nextInt(64)];
			random.nextBytes(buf);
			// Start with a list or dictionary to reach nested parsing
			buf[0] = random.nextBoolean() ? Types.LIST : Types.DICTIONARY;
			parse(buf, i);
		}
	}

	@Test
	public void testMutatedValidInput() throws Exception {
		Random random = new Random(0x4e43_0002L);
		byte[] valid = encodeSample();
		int parsed = 0;
		for (int i = 0; i < ITERATIONS; i++) {
			byte[] buf = Arrays.copyOf(valid,
					valid.length - random.nextInt(4));
			int flips = 1 + random.nextInt(3);
			for (int f = 0; f < flips; f++) {
				buf[random.nextInt(buf.length)] = (byte) random.nextInt(256);
			}
			if (parse(buf, i)) parsed++;
		}
		// Some mutations (e.g. inside string or raw contents) still parse,
		// which shows the inputs reach deep into the parser
		assertTrue("parsed " + parsed, parsed > ITERATIONS / 100);
	}

	@Test
	public void testDeepNestingIsBounded() throws Exception {
		byte[] buf = new byte[100_000];
		Arrays.fill(buf, Types.LIST);
		parse(buf, -1);
	}

	@Test
	public void testHugeDeclaredLengthIsBounded() throws Exception {
		// A raw value claiming a 2^31 - 1 byte length, with no data
		byte[] buf = {Types.LIST, Types.RAW_32, 0x7F, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF};
		parse(buf, -1);
	}

	/**
	 * Returns true if the input parsed, false if it was rejected.
	 */
	private static boolean parse(byte[] buf, int iteration) {
		BdfReaderImpl r = new BdfReaderImpl(new ByteArrayInputStream(buf),
				DEFAULT_NESTED_LIMIT, DEFAULT_MAX_BUFFER_SIZE, true);
		try {
			if (buf[0] == Types.DICTIONARY) r.readDictionary();
			else r.readList();
			return true;
		} catch (FormatException | EOFException expected) {
			// Malformed input is rejected
			return false;
		} catch (Throwable t) {
			fail("Iteration " + iteration + " input "
					+ Arrays.toString(buf) + " threw " + t);
			return false;
		}
	}

	private static byte[] encodeSample() throws Exception {
		Map<String, Object> inner = new HashMap<>();
		inner.put("n", 1234567L);
		inner.put("raw", new byte[] {1, 2, 3, 4, 5});
		inner.put("f", 3.25);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BdfWriterImpl w = new BdfWriterImpl(out);
		w.writeList(Arrays.asList("text", true, null, -42L, inner,
				Arrays.asList(1L, 2L, "three")));
		w.flush();
		return out.toByteArray();
	}
}
