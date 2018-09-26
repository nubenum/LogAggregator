package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.layers.entries.HostSourceGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.StackedEntry;

public class HostSourceGroupedLogTest {
	private static IEntryLog file;

	private static final IEntry[] entries = {
			new LinedEntry("2018-07-02 16:10:31:466 a"),
			new LinedEntry("b"),
			new LinedEntry("c"),
			new LinedEntry("d"),
			new LinedEntry("2018-08-02 16:10:31:466 a"),
			new LinedEntry("2018-08-02 16:10:31:467 b"),
			new LinedEntry("c"),
			new LinedEntry("d"),
			new LinedEntry("2018-08-02 16:10:32:466 a"),
			new LinedEntry("2018-08-02 16:10:33:466 a"),
			new LinedEntry("2018-08-02 16:10:34:466 a"),
			new LinedEntry("2018-08-02 16:10:35:466 a"),
			new LinedEntry("2018-08-02 16:10:36:466 a"),
			new LinedEntry("2018-08-02 16:10:37:466 a"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
			new LinedEntry("b"),
	};

	@BeforeClass
	public static void setup() {
		file = new TestHelper.AbstractEntryLog() {
			@Override
			public IEntry getAt(IEntry reference, int offset) throws IOException {
				return TestHelper.getAt(reference, offset, entries);
			}
		};
	}

	@Test
	public void testUpDown() throws IOException, InterruptedException {
		HostSourceGroupedLog log = new HostSourceGroupedLog(file);
		IEntry entry = log.getAt(entries[4], -1);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(4, entry.getChildren().size());
		assertEquals(entries[0].getLogTime(), entry.getLogTime());

		entry = log.getAt(entry, 1);
		assertEquals(entries[4], entry);
	}

	@Test
	public void testDownUp() throws IOException, InterruptedException {
		HostSourceGroupedLog log = new HostSourceGroupedLog(file);
		IEntry entry = log.getAt(entries[4], 1);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(3, entry.getChildren().size());
		assertEquals(entries[5].getLogTime(), entry.getLogTime());

		entry = log.getAt(entry, -1);
		assertEquals(entries[4], entry);
	}

	@Test
	public void testDownUpRand() throws IOException, InterruptedException {
		HostSourceGroupedLog log = new HostSourceGroupedLog(file);
		IEntry entry = log.getAt(entries[12], 1);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(12, entry.getChildren().size());
		assertEquals(entries[13].getLogTime(), entry.getLogTime());

		entry = log.getAt(entry, -2);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(4, entry.getChildren().size());
		assertEquals(entries[0].getLogTime(), entry.getLogTime());

		entry = log.getAt(entries[10], -2);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(4, entry.getChildren().size());
		assertEquals(entries[0].getLogTime(), entry.getLogTime());
	}

	@Test
	public void testUpDownRandAndSelf() throws IOException, InterruptedException {
		HostSourceGroupedLog log = new HostSourceGroupedLog(file);
		IEntry entry = log.getAt(entries[12], -2);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(4, entry.getChildren().size());
		assertEquals(entries[0].getLogTime(), entry.getLogTime());

		entry = log.getAt(entry, 2);
		assertTrue(entry instanceof StackedEntry);
		assertEquals(12, entry.getChildren().size());
		assertEquals(entries[13].getLogTime(), entry.getLogTime());

		entry = log.getAt(entry, 2);
		assertEquals(Entry.LAST, entry);
	}

}
