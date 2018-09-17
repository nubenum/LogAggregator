package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageLink;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageMatch;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.Level;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;

public class LinedEntryTest {

	@Test
	public void test() {
		IEntry entry = new LinedEntry("2018-06-02 16:10:31:466 [INFO] lorem", new FileRange(1, 42), null, null);
		assertTrue(entry.getLogTime().equals(new LogTime(2018,6,2,16,10,31,466)));
		assertEquals(new FileRange(1, 42), entry.getRange());
		assertEquals("lorem", entry.getMessage());
		assertEquals(Level.INFO, entry.getLevel());

		entry = new LinedEntry("[UNKNOWN] lorem", new FileRange(1, 42), null, null);
		assertNull(entry.getLogTime());
		assertEquals("lorem", entry.getMessage());
		assertEquals(entry.getLevel(), Level.OTHER);
	}

	@Test
	public void testTimestamps() {
		IEntry entry;
		entry = new LinedEntry("[5/22/18 14:55:04:802 CEST] abc", new FileRange(1, 42), null, null);
		assertTrue(entry.getLogTime().equals(new LogTime(2018,5,22,14,55,4,802)));
		assertEquals("abc", entry.getMessage());
		assertNull(entry.getLevel());

		entry = new LinedEntry("1523052003756;2018;4;7;0;0;3;756;abc", new FileRange(1, 42), null, null);
		assertTrue(entry.getLogTime().equals(new LogTime(2018, 4, 7, 0, 0, 3, 756)));
		assertEquals("2018;4;7;0;0;3;756;abc", entry.getMessage());
		assertNull(entry.getLevel());

		entry = new LinedEntry("2017-07-18 09:04:47.289;abc", new FileRange(1, 42), null, null);
		assertTrue(entry.getLogTime().equals(new LogTime(2017, 7, 18, 9, 4, 47, 289)));
		assertEquals("abc", entry.getMessage());
		assertNull(entry.getLevel());
	}

	@Test
	public void compareToTest() {
		ILogHost h1 = mock(ILogHost.class);
		when(h1.getName()).thenReturn("a");

		ILogHost h2 = mock(ILogHost.class);
		when(h2.getName()).thenReturn("b");

		ILogSource s1 = mock(ILogSource.class);
		when(s1.getName()).thenReturn("a");

		ILogSource s2 = mock(ILogSource.class);
		when(s2.getName()).thenReturn("b");


		LinedEntry a = new LinedEntry("2018-06-02 16:10:31:465 [INFO] lorem", new FileRange(1, 42), null, null);
		LinedEntry b = new LinedEntry("2018-06-02 16:10:31:466 [INFO] lorem", new FileRange(43, 42), null, null);
		LinedEntry c = new LinedEntry("2018-06-02 16:10:31:466 [INFO] lorem", new FileRange(43, 42), null, null);
		LinedEntry d = new LinedEntry("2018-06-02 16:10:31:466 [INFO] lorem", new FileRange(40, 42), h1, s2);
		LinedEntry e = new LinedEntry("2018-06-02 16:10:31:466 [INFO] lorem", new FileRange(40, 42), h2, s1);
		LinedEntry f = new LinedEntry("2018-06-02 16:10:31:466 [INFO] lorem", new FileRange(40, 42), h1, s1);
		assertEquals(0, a.compareTo(a));
		assertEquals(-1, a.compareTo(Entry.LAST));
		assertEquals(1, a.compareTo(Entry.FIRST));
		assertEquals(-1, Entry.FIRST.compareTo(Entry.LAST));
		assertEquals(1, Entry.LAST.compareTo(Entry.FIRST));
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
		assertEquals(0, b.compareTo(c));
		assertEquals(1, c.compareTo(d));
		assertEquals(-1, d.compareTo(e));
		assertEquals(1, d.compareTo(f));
	}


	@Test
	public void getLinksTest() {
		LinedEntry a = new LinedEntry("2018-06-02 16:10:31:465 [INFO]   at example.common.Test.execute(Test.java:117)", new FileRange(1, 42), null, null);
		EntryMessageLink l = a.getLinks().get(0);
		assertEquals("Test", l.getLinkedClass());
		assertEquals("example.common", l.getLinkedPackage());
		assertEquals("execute", l.getLinkedMethod());
		assertEquals(117, l.getLinkedLine());
		assertEquals(64, l.getStart());
		assertEquals(77, l.getEnd());
	}

	@Test
	public void getMatchesTest() {
		LinedEntry a = new LinedEntry("2018-06-02 16:10:31:465 [INFO]   at example.common.Test.execute(Test.java:117)", new FileRange(1, 42), null, null);
		IEntryMatcher f = new EntryMatcher(Level.ALL, "execute", IEntry.class);
		EntryMessageMatch l = a.getMatches(f, true).get(0);
		assertEquals(25, l.getStart());
		assertEquals(32, l.getEnd());
	}

}
