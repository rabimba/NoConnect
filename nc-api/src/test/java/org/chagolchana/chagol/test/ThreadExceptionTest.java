package org.chagolchana.chagol.test;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ThreadExceptionTest extends ChagolTestCase {

	@Test(expected = AssertionError.class)
	public void testAssertionErrorMakesTestCaseFail() {
		// This is what ChagolTestCase does, too:
		fail();
	}

	@Test
	public void testExceptionInThreadMakesTestCaseFail() {
		Thread t = new Thread(() -> {
			System.out.println("thread before exception");
			throw new RuntimeException("boom");
		});

		t.start();
		try {
			t.join();
			System.out.println("joined thread");
		} catch (InterruptedException e) {
			System.out.println("interrupted while joining thread");
			fail();
		}

		assertNotNull(exceptionInBackgroundThread);
		exceptionInBackgroundThread = null;
	}

}
