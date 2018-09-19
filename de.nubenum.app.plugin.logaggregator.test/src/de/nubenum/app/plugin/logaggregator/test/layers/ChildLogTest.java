package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.layers.AbstractChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;

public class ChildLogTest {
	private static AbstractChildLog log;
	private static final IEntry[] entries = {
			new LinedEntry("2018-06-02 16:10:31:466 a"),
			new LinedEntry("2018-07-02 16:10:31:466 b"),
			new LinedEntry("2018-08-02 16:10:31:466 b"),
			new LinedEntry("2018-10-02 16:10:31:466 b"),
			new LinedEntry("2018-10-02 16:10:31:467 c"),
			new LinedEntry("2018-10-02 16:10:31:468 d"),
			new LinedEntry("2018-11-02 16:10:31:466 e")
	};

	@BeforeClass
	public static void setup() {
		IEntryLog entryLog = new IEntryLog() {
			@Override
			public IEntry getAt(IEntry reference, int offset) throws IOException {
				return TestHelper.getAt(reference, offset, entries);
			}
		};
		log = new AbstractChildLog(entryLog) {
			@Override
			public IEntry getAt(IEntry reference, int offset) throws IOException, InterruptedException {
				return super.getAtBest(reference, offset);
			}

			@Override
			public boolean isOwnEntry(IEntry reference) {
				return false;
			}
		};
	}

	@Test
	public void test() throws IOException, InterruptedException {
		IEntry entry = log.getAt(Entry.FIRST, 1);
		assertEquals(entries[0], entry);

		entry = log.getAt(entries[5], 1);
		assertEquals(entries[6], entry);

		entry = log.getAt(new LinedEntry("2018-10-02 16:10:31:466 c"), 1);
		assertEquals(entries[4], entry);

		entry = log.getAt(new LinedEntry("2018-06-02 16:10:31:467 c"), -1);
		assertEquals(entries[0], entry);

		entry = log.getAt(new LinedEntry("2018-07-02 16:09:31:466 c"), -1);
		assertEquals(entries[0], entry);

		entry = log.getAt(new LinedEntry("2019-03-02 16:10:31:466 c"), -1);
		assertEquals(entries[6], entry);

		entry = log.getAt(Entry.LAST, -1);
		assertEquals(entries[6], entry);
	}

}
