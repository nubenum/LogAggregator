package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.layers.HostGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostParentLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostSourceChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostSourceGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.LinedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.LocalRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.RotatedRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

public class MergeIntegrationTest {

	private static IEntryLog premerged;
	private static IEntryLog processed;

	private static Path getPath(String name) {
		return Paths.get("testdata/", name+".log");
	}

	@BeforeClass
	public static void setup() throws IOException {
		ILogSource src1 = mock(ILogSource.class);
		when(src1.getName()).thenReturn("audit");
		List<IRandomAccessLog> list1 = Arrays.asList(new LocalRandomAccessLog(getPath(src1.getName())));
		IChildLog child1 = new HostSourceChildLog(new HostSourceGroupedLog(new LinedLog(new RotatedRandomAccessLog(list1), null, src1)), src1);

		ILogSource src2 = mock(ILogSource.class);
		when(src2.getName()).thenReturn("info");
		List<IRandomAccessLog> list2 = Arrays.asList(new LocalRandomAccessLog(getPath(src2.getName())));
		IChildLog child2 = new HostSourceChildLog(new HostSourceGroupedLog(new LinedLog(new RotatedRandomAccessLog(list2), null, src2)), src2);

		processed = new HostGroupedLog(new HostParentLog(Arrays.asList(child1, child2)));

		ILogSource src3 = mock(ILogSource.class);
		when(src3.getName()).thenReturn("premerged");
		List<IRandomAccessLog> list3 = Arrays.asList(new LocalRandomAccessLog(getPath(src3.getName())));
		premerged = new HostGroupedLog(new HostSourceGroupedLog(new LinedLog(new RotatedRandomAccessLog(list3), null, src3)));

	}

	@Test
	public void testContinuousDown() throws IOException {
		IEntry entry1 = Entry.FIRST;
		IEntry entry2 = Entry.FIRST;
		while(true) {
			entry1 = premerged.getAt(entry1, 1);
			entry2 = processed.getAt(entry2, 1);

			//can't use equals since in different sources
			//System.out.println(entry1+"|"+entry2);
			assertTrue(entry1+"|"+entry2, entry1.getLogTime().equals(entry2.getLogTime()));
			assertEquals(entry1.getMessage(), entry2.getMessage());
			assertEquals(entry1.getChildren().size(), entry2.getChildren().size());

			if (Entry.isFirstOrLast(entry1)) {
				return;
			}
		}
	}

	@Test
	public void testContinuousUp() throws IOException {
		IEntry entry1 = Entry.LAST;
		IEntry entry2 = Entry.LAST;

		while(true) {
			entry1 = premerged.getAt(entry1, -1);
			entry2 = processed.getAt(entry2, -1);
			assertTrue(entry1+"|"+entry2, entry1.getLogTime().equals(entry2.getLogTime()));
			assertEquals(entry1.getMessage(), entry2.getMessage());
			assertEquals(entry1.getChildren().size(), entry2.getChildren().size());

			if (Entry.isFirstOrLast(entry1)) {
				return;
			}
		}
	}

	@Test
	public void testRandomDown() throws IOException {
		IEntry entry1 = Entry.FIRST;
		IEntry entry2 = Entry.FIRST;
		int i = 1;

		while(true) {
			i *= 2;
			if (i > 1000) i /= 10;
			System.out.println(i);
			entry1 = premerged.getAt(entry1, i);
			entry1 = premerged.getAt(entry1, 1);
			entry1 = premerged.getAt(entry1, 1);
			entry2 = processed.getAt(entry2, i);
			entry2 = processed.getAt(entry2, 1);
			entry2 = processed.getAt(entry2, 1);
			if (Entry.isFirstOrLast(entry1))
				break;
		}
	}

	@Test
	public void testRandomUp() throws IOException {
		IEntry entry1 = Entry.LAST;
		IEntry entry2 = Entry.LAST;
		int i = 1;
		while(true) {
			i *= 2;
			if (i > 1000) i /= 10;
			System.out.println(i);
			entry1 = premerged.getAt(entry1, -i);
			entry1 = premerged.getAt(entry1, -1);
			entry1 = premerged.getAt(entry1, -1);
			entry2 = processed.getAt(entry2, -i);
			entry2 = processed.getAt(entry2, -1);
			entry2 = processed.getAt(entry2, -1);
			if (Entry.isFirstOrLast(entry1))
				break;
		}
	}
}
