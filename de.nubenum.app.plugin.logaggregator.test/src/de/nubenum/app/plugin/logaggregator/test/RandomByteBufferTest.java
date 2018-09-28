package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;

public class RandomByteBufferTest {

	@Test
	public void testConcat() {
		RandomByteBuffer b1 = new RandomByteBuffer("abc".getBytes());
		RandomByteBuffer b2 = new RandomByteBuffer("def".getBytes());
		RandomByteBuffer buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(0, 20), 3));
		RandomByteBuffer buf2 = new RandomByteBuffer(b2, new FileRange(new FilePosition(1, 0), 3));
		buf1.concatOrdered(buf2);
		assertEquals("abcdef", new String(buf1.getBytes()));
		assertEquals("abcdef", new String(buf1.getOffsetBytes()));
		assertEquals(buf1.getRange(), new FileRange(new FilePosition(0, 20), 6));
		assertEquals(buf1.getOffset(), 0);
		assertEquals(buf1.getLength(), 6);
		assertEquals(buf1.getOffsetLength(), 6);
		assertEquals(buf1.getRange().inRange(new FilePosition(0, 24)), true);
		assertEquals("def", new String(buf2.getBytes()));
		assertEquals("def", new String(buf2.getOffsetBytes()));
		assertEquals(buf2.getRange(), new FileRange(new FilePosition(1, 0), 3));
		assertEquals(buf2.getOffset(), 0);
		assertEquals(buf2.getLength(), 3);
		assertEquals(buf2.getOffsetLength(), 3);
	}

	@Test
	public void testOffsetDownConcat() {
		RandomByteBuffer b1 = new RandomByteBuffer("abc".getBytes());
		RandomByteBuffer b2 = new RandomByteBuffer("def".getBytes());

		RandomByteBuffer buf1;
		RandomByteBuffer buf2;
		buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(0, 20), 3), 2, Direction.DOWN);
		buf2 = new RandomByteBuffer(b2, new FileRange(new FilePosition(0, 23), 3));

		assertEquals(buf1.getOffsetLength(), 1);
		buf1.concatOrdered(buf2);
		assertEquals(buf1.getRange(), new FileRange(new FilePosition(0, 20), 6));
		assertEquals(buf1.getOffset(), 2);
		assertEquals(buf1.getOffsetLength(), 4);
		assertEquals("abcdef", new String(buf1.getBytes()));
		assertEquals("cdef", new String(buf1.getOffsetBytes()));

		buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(0, 20), 3), 1, Direction.DOWN);
		buf2 = new RandomByteBuffer(b2, new FileRange(new FilePosition(0, 23), 3), 2, Direction.DOWN);

		assertEquals(buf1.getOffsetLength(), 2);
		buf1.concatOrdered(buf2);
		assertEquals(buf1.getRange(), new FileRange(new FilePosition(0, 20), 4));
		assertEquals(buf1.getOffset(), 1);
		assertEquals(buf1.getOffsetLength(), 3);
		assertEquals("abcf", new String(buf1.getBytes()));
		assertEquals("bcf", new String(buf1.getOffsetBytes()));
	}

	@Test
	public void testOffsetUpConcat() {
		RandomByteBuffer b1 = new RandomByteBuffer("abc".getBytes());
		RandomByteBuffer b2 = new RandomByteBuffer("def".getBytes());

		RandomByteBuffer buf1;
		RandomByteBuffer buf2;

		buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(1, 0), 3), 1, Direction.UP);
		buf2 = new RandomByteBuffer(b2, new FileRange(new FilePosition(0, 23), 3), 2, Direction.UP);
		buf1.concatOrdered(buf2);
		assertEquals(buf1.getRange(), new FileRange(new FilePosition(0, 23), 6));
		assertEquals(buf1.getOffset(), 4);
		assertEquals(buf1.getOffsetLength(), 5);
		assertEquals("defabc", new String(buf1.getBytes()));
		assertEquals("defab", new String(buf1.getOffsetBytes()));

		buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(0, 26), 3), 1, Direction.UP);
		buf2 = new RandomByteBuffer(b2, new FileRange(new FilePosition(0, 23), 3), 0, Direction.UP);

		buf1.concatOrdered(buf2);
		assertEquals(buf1.getRange(), new FileRange(new FilePosition(0, 23), 4));
		assertEquals(buf1.getOffset(), 2);
		assertEquals(buf1.getOffsetLength(), 3);
		assertEquals("dabc", new String(buf1.getBytes()));
		assertEquals("dab", new String(buf1.getOffsetBytes()));
	}

	@Test
	public void testNotAdjacent() {
		byte[] b1 = "abc".getBytes();
		byte[] b2 = "def".getBytes();
		RandomByteBuffer buf1 = new RandomByteBuffer(b1);
		RandomByteBuffer buf2 = new RandomByteBuffer(b2);
		try {
			buf1.concatOrdered(buf2);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void testOtherDirection() {
		RandomByteBuffer b1 = new RandomByteBuffer("abc".getBytes());
		RandomByteBuffer b2 = new RandomByteBuffer("def".getBytes());

		RandomByteBuffer buf1;
		RandomByteBuffer buf2;

		buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(1, 0), 3), 1, Direction.UP);
		buf2 = new RandomByteBuffer(b2, new FileRange(new FilePosition(0, 23), 3), 2, Direction.DOWN);
		try {
			buf1.concatOrdered(buf2);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void testOffsetOutOfBounds() {
		RandomByteBuffer b1 = new RandomByteBuffer("abc".getBytes());
		RandomByteBuffer buf1;

		try {
			buf1 = new RandomByteBuffer(b1, new FileRange(new FilePosition(1, 0), 3), 3, Direction.UP);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) {

		}
	}

}
