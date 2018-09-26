package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.layers.entries.FilteredLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.IFilteredLog;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.Level;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;
import de.nubenum.app.plugin.logaggregator.core.model.entry.DeduplicatedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.StackedEntry;

public class FilteredLogTest {
	private static IFilteredLog log;

	private static final IEntry[] entries = {
			new LinedEntry("2018-07-02 16:10:31:466 [FATAL] abc"),
			new LinedEntry("2018-08-02 16:10:31:466 [WARNING] Lorim"),
			new StackedEntry(Arrays.asList(new LinedEntry("2018-09-01 16:10:31:466 Lorem"), new LinedEntry("abc"))),
			new LinedEntry("2018-09-02 16:10:31:468 [FINEST] abc"),
			new DeduplicatedEntry(Arrays.asList(
					new StackedEntry(
							Arrays.asList(new LinedEntry("2018-10-02 16:10:31:466 [OTHER] abc"), new LinedEntry("def"))
							))),
			new LinedEntry("2018-11-02 16:10:31:466 ipsum Lorem"),
			new LinedEntry("2018-12-02 16:10:31:466 [INFO] abc")
	};

	@BeforeClass
	public static void setup() {
		log = new FilteredLog();
		log.setLog(new IEntryLog() {
			@Override
			public IEntry getAt(IEntry reference, int offset) throws IOException {
				return TestHelper.getAt(reference, offset, entries);
			}

			@Override
			public void close() {
				// TODO Auto-generated method stub

			}

			@Override
			public void close(boolean keepInit) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Test
	public void testLevels() throws IOException, InterruptedException {
		log.setMatcher(new EntryMatcher(Level.INFO, "", IEntry.class));
		log.toggleFilter(true);

		IEntry entry = log.getAt(Entry.LAST, -1);
		assertEquals(entries[6], entry);
		entry = log.getAt(entry, -1);
		assertEquals(entries[4], entry);
		entry = log.getAt(entry, -1);
		assertEquals(entries[1], entry);
		entry = log.getAt(entry, -1);
		assertEquals(entries[0], entry);
	}

	@Test
	public void testStacks() throws IOException, InterruptedException {
		log.setMatcher(new EntryMatcher(Level.ALL, "", StackedEntry.class));
		log.toggleFilter(true);

		IEntry entry = log.getAt(Entry.FIRST, 1);
		assertEquals(entries[2], entry);
		entry = log.getAt(entry, 1);
		assertEquals(entries[4], entry);

		entry = log.getAt(entry, 1);
		assertEquals(Entry.LAST, entry);
	}

	@Test
	public void testRegex() throws IOException, InterruptedException {
		log.setMatcher(new EntryMatcher(Level.ALL, "^Lor[ei]m", IEntry.class));
		log.toggleFilter(true);

		IEntry entry = log.getAt(Entry.FIRST, 1);
		assertEquals(entries[1], entry);
		entry = log.getAt(entry, 1);
		assertEquals(entries[2], entry);

		entry = log.getAt(entry, 1);
		assertEquals(Entry.LAST, entry);
	}

	@Test
	public void testCombined() throws IOException, InterruptedException {
		log.setMatcher(new EntryMatcher(Level.OTHER, "Lor", IEntry.class));
		log.toggleFilter(true);

		IEntry entry = log.getAt(Entry.LAST, -1);
		assertEquals(entries[1], entry);

		entry = log.getAt(entry, -1);
		assertEquals(Entry.FIRST, entry);
	}

	@Test
	public void testMatching() throws IOException, InterruptedException {
		log.setMatcher(new EntryMatcher(Level.OTHER, "bc$", IEntry.class));
		log.toggleFilter(false);

		ReferenceOffset pair = log.getMatchingAt(Entry.LAST, -1);
		assertEquals(entries[4], pair.getEntry());
		assertEquals(-3, pair.getOffset());

		pair = log.getMatchingAt(pair.getEntry(), -1);
		assertEquals(entries[0], pair.getEntry());
		assertEquals(-4, pair.getOffset());

		pair = log.getMatchingAt(pair.getEntry(), -1);
		assertEquals(Entry.FIRST, pair.getEntry());
	}

}
