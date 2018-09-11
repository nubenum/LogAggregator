package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.layers.AbstractGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.CondensedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;

public class GroupedLogTest {
	private static AbstractGroupedLog log;
	private static final IEntry[] entries = {
			new LinedEntry("2018-07-02 16:10:31:466 a"),
			new LinedEntry("2018-08-02 16:10:31:466 b"),
			new LinedEntry("2018-08-02 16:10:31:467 b"),
			new LinedEntry("2018-08-02 16:10:31:468 b"),
			new LinedEntry("2018-10-02 16:10:31:466 c"),
			new LinedEntry("2018-11-02 16:10:31:466 d"),
			new LinedEntry("2018-12-02 16:10:31:466 e")
	};

	@BeforeClass
	public static void setup() {
		IEntryLog entryLog = new IEntryLog() {

			@Override
			public IEntry getAt(IEntry reference, int offset) throws IOException {
				return TestHelper.getAt(reference, offset, entries);
			}
		};
		log = new AbstractGroupedLog(entryLog) {

			@Override
			protected IEntry makeGroup(List<IEntry> entries) {
				return new CondensedEntry(entries);
			}

			@Override
			protected boolean isGroupable(List<IEntry> entries, IEntry next, Direction dir) {
				return entries.get(entries.size()-1).getMessage().equals(next.getMessage());
			}

			@Override
			protected int getAvgGroupSize() {
				return 2;
			}

			@Override
			protected IEntry degroupedReference(IEntry reference, Direction dir) {
				if (reference instanceof CondensedEntry)
					return ((CondensedEntry) reference).getBoundChild(dir);
				return reference;
			}
		};
	}

	@Test
	public void test() throws IOException {

		IEntry entry = log.getAt(entries[1], -1);
		assertTrue(entry instanceof LinedEntry);
		assertEquals(entries[0], entry);

		entry = log.getAt(entries[0], 1);
		assertTrue(entry instanceof CondensedEntry);
		assertEquals(3, entry.getChildren().size());

		entry = log.getAt(entry, -1);
		assertEquals(entries[0], entry);

		entry = log.getAt(entries[0], 1); //cache
		assertTrue(entry instanceof CondensedEntry);
		assertEquals(3, entry.getChildren().size());

		entry = log.getAt(entry, 1);
		assertEquals(entries[4], entry);

		entry = log.getAt(entries[6], -2);
		assertTrue(entry instanceof CondensedEntry);
		assertEquals(3, entry.getChildren().size());

		entry = log.getAt(entries[0], 3);
		assertTrue(entry instanceof LinedEntry);
		assertEquals(entries[6], entry);

		entry = log.getAt(entries[5], 1);
		assertTrue(entry instanceof LinedEntry);
		assertEquals(entries[6], entry);

		entry = log.getAt(entries[5], 10);
		assertEquals(Entry.LAST, entry);


	}

}
