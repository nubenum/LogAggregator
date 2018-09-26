package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.layers.AbstractParentLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IChildLog;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;

public class ParentLogTest {
	private static class TestChildLog extends TestHelper.AbstractEntryLog implements IChildLog {
		public IEntry[] entries;
		public TestChildLog(IEntry[] entries) {
			this.entries = entries;
		}
		@Override
		public IEntry getAt(IEntry reference, int offset) throws IOException {
			return TestHelper.getAt(reference, offset, entries);
		}
		@Override
		public boolean isOwnEntry(IEntry reference) {
			return Arrays.asList(entries).indexOf(reference) != -1;
		}
	};
	private static List<TestChildLog> list;
	private static AbstractParentLog log;
	private static IEntry[] l1 = new IEntry[] {
			new LinedEntry("2018-07-02 16:10:31:466 b"),
			new LinedEntry("2018-08-02 16:10:31:466 b"),
			new LinedEntry("2018-10-02 16:10:31:466 b"),
			new LinedEntry("2018-10-02 16:10:31:467 c"),
			new LinedEntry("2018-11-02 16:10:31:466 e")
	};
	private static IEntry[] l2 = new IEntry[] {
			new LinedEntry("2018-06-02 16:10:31:466 a"),
			new LinedEntry("2018-08-02 17:10:31:466 b"),
			new LinedEntry("2018-10-02 16:10:30:466 b"),
			new LinedEntry("2018-12-02 16:10:31:466 e")
	};
	@BeforeClass
	public static void setup() {

		list = new ArrayList<>();
		list.add(new TestChildLog(l1));
		list.add(new TestChildLog(l2));
		log = new AbstractParentLog(list) {};
	}

	@Test
	public void test() throws IOException, InterruptedException {
		IEntry entry = log.getAt(l1[1], -1);
		assertEquals(l1[0], entry);

		entry = log.getAt(l1[0], 1);
		assertEquals(l2[0], entry);

		entry = log.getAt(l1[0], 3);
		assertEquals(l1[3], entry);

		entry = log.getAt(Entry.LAST, -1);
		assertEquals(l2[3], entry);

		entry = log.getAt(Entry.FIRST, 2);
		assertEquals(l1[1], entry);
	}

}
