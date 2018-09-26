package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.layers.IRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.LinedLog;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.IFileRange;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;

public class LinedLogTest {
	private static IRandomAccessLog rand;

	public static String longLine = "9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdfh9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdfh9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdfh9reh97rht9hisdf9847riduh9h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdf9847riduh9qz4rt h9reh97rht9hisdfh9reh97rht9hisdf984g";

	@BeforeClass
	public static void setup() {
		rand = new IRandomAccessLog() {
			public byte[] data = ("Lorem0 ipsum döor sit amet\n"
					+ "amet sit dolor ipsum0 Lorem\n"
					+ "Lorem1 ipsum dolor sit amet\n"
					+ longLine + "\n").getBytes(StandardCharsets.UTF_8);

			@Override
			public long getLength() throws IOException {
				return data.length;
			}

			@Override
			public RandomByteBuffer getAt(IFilePosition start, Direction dir) throws IOException, EndOfLogReachedException {
				if (start.getByteOffset() < 0)
					throw new EndOfLogReachedException(Direction.UP);
				if (start.getByteOffset() >= getLength())
					throw new EndOfLogReachedException(Direction.DOWN);

				if (start == FilePosition.FIRST) {
					start = new FilePosition(0, 0);
				} else if (start == FilePosition.LAST) {
					start = new FilePosition(0, getLength()-1);
				}

				int len = dir.getValue()*20;
				IFileRange range = new FileRange(start, len).clip(getLength());
				int top = (int) range.getTop().getByteOffset();
				int bottom = (int) range.getBottom().getByteOffset();
				RandomByteBuffer r = new RandomByteBuffer(
						new RandomByteBuffer(Arrays.copyOfRange(data, top, bottom)),
						range,
						dir == Direction.UP ? bottom-top-1 : 0, dir);
				return r;
			}

			@Override
			public void close() {
				return;
			}

			@Override
			public long getLength(boolean forceRefresh) throws IOException {
				return 0;
			}
		};
	}

	@Test
	public void testInBounds() throws IOException {
		LinedLog lined = new LinedLog(rand, null, null);
		LinedEntry ref = new LinedEntry("a", new FileRange(56L, 27), null, null);
		IEntry entry = lined.getAt(ref, -1);
		assertEquals("amet sit dolor ipsum0 Lorem", entry.getMessage());
		assertEquals(new FileRange(28, 27), entry.getRange());
		entry = lined.getAt(ref, 1);
		assertEquals(longLine, entry.getMessage());
		assertEquals(new FileRange(84, 9214), entry.getRange());
	}

	@Test
	public void testFirstLast() throws IOException {
		LinedLog lined = new LinedLog(rand, null, null);
		LinedEntry ref = new LinedEntry("a", new FileRange(56L, 27), null, null);

		IEntry entry = lined.getAt(Entry.FIRST, 1);
		assertEquals("Lorem0 ipsum döor sit amet", entry.getMessage());
		assertEquals(new FileRange(0, 27), entry.getRange());
		entry = lined.getAt(Entry.LAST, -1);
		assertEquals(longLine, entry.getMessage());
		assertEquals(new FileRange(84, 9214), entry.getRange());

		entry = lined.getAt(Entry.LAST, 1);
		assertEquals(Entry.LAST, entry);

		entry = lined.getAt(Entry.FIRST, -1);
		assertEquals(Entry.FIRST, entry);
	}

	@Test
	public void testOutBounds() throws IOException {
		LinedLog lined = new LinedLog(rand, null, null);
		LinedEntry ref = new LinedEntry("a", new FileRange(56L, 27), null, null);

		IEntry entry = lined.getAt(ref, 2);
		assertEquals(Entry.LAST, entry);

		entry = lined.getAt(ref, -1);
		assertEquals("amet sit dolor ipsum0 Lorem", entry.getMessage());
		assertEquals(new FileRange(28, 27), entry.getRange());
		entry = lined.getAt(entry, -1);

		assertEquals("Lorem0 ipsum döor sit amet", entry.getMessage());
		assertEquals(new FileRange(0, 27), entry.getRange());
		entry = lined.getAt(entry, -1);
		assertEquals(Entry.FIRST, entry);
	}

	@Test
	public void testSelf() throws IOException {
		LinedLog lined = new LinedLog(rand, null, null);
		LinedEntry ref = new LinedEntry("a", new FileRange(56L, 27), null, null);

		IEntry entry = lined.getAt(ref, 0);
		assertTrue(entry == ref);

	}

}
