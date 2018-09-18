package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.IFileRange;

public class FileRangeTest {

	@Test
	public void test() {
		IFilePosition pos1 = new FilePosition(0, 10);
		IFilePosition pos2 = new FilePosition(1, 3);
		IFilePosition pos3 = new FilePosition(1, 5);
		IFilePosition pos4 = new FilePosition(1, 5);
		assertEquals(-1, pos1.compareTo(pos2));
		assertEquals(-1, pos2.compareTo(pos3));
		assertEquals(1, pos3.compareTo(pos2));
		assertEquals(0, pos4.compareTo(pos3));
		assertEquals(2, pos3.distance(pos2));
		assertEquals(-2, pos2.distance(pos3));

		IFileRange range1 = new FileRange(pos1, 10);
		assertEquals(new FilePosition(0,10), range1.getTop());
		assertEquals(new FilePosition(0,20), range1.getBottom());
		assertEquals(new FilePosition(0,20), range1.getNext(Direction.DOWN));
		assertEquals(new FilePosition(0,9), range1.getNext(Direction.UP));

		range1 = new FileRange(pos1, 5, Direction.UP);
		assertEquals(new FilePosition(0,6), range1.getTop());
		assertEquals(new FilePosition(0,11), range1.getBottom());
		assertEquals(new FilePosition(0,11), range1.getNext(Direction.DOWN));
		assertEquals(new FilePosition(0,5), range1.getNext(Direction.UP));
		assertTrue(range1.inRange(new FilePosition(0,10)));
		assertFalse(range1.inRange(new FilePosition(0,11)));
		assertTrue(range1.inRange(new FilePosition(0,6)));
		assertFalse(range1.inRange(new FilePosition(0,5)));

		range1 = new FileRange(10, 10);
		assertEquals(new FilePosition(0,10), range1.getTop());
		assertEquals(new FilePosition(0,20), range1.getBottom());
	}

}
