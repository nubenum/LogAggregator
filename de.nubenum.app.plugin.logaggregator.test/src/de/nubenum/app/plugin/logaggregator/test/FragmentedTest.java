package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.Bench;
import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.layers.bytes.IRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.bytes.LocalRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.bytes.RotatedRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.dirs.LocalLogDirectory;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.AggregatedChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.AggregatedGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.AggregatedParentLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.FilteredLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.HostChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.HostParentLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.HostSourceChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.HostSourceGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.IChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.IFilteredLog;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.LinedLog;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

public class FragmentedTest {

	private static IChildLog processed;
	private static IFilteredLog allLayers;

	private static LogTime[] timeJournal = new LogTime[] {
			LogTime.MIN,
			new	LogTime(2018,4,30,6,41,47,975),
			new	LogTime(2018,5,2,8,7,22,863),
			new	LogTime(2018,5,7,7,11,44,766),
			new	LogTime(2018,5,7,11,38,44,238),
	};
	private static int[] sizeJournal = new int[] {
			3, 7, 16, 0, 7
	};

	@BeforeClass
	public static void setup() throws IOException {
		ILogHost host1 = mock(ILogHost.class);
		when(host1.getName()).thenReturn("testdata/fragmented/");

		ILogSource src1 = mock(ILogSource.class);
		when(src1.getName()).thenReturn("SystemErr");
		LocalLogDirectory dir = new LocalLogDirectory("./", host1, src1);
		List<IRandomAccessLog> list1 = dir.getSourceFiles(src1).stream().map(f -> {
			return new LocalRandomAccessLog(f);
		}).collect(Collectors.toList());
		LinedLog lined = new LinedLog(new RotatedRandomAccessLog(list1), null, src1);
		processed = new HostSourceChildLog(new HostSourceGroupedLog(lined), src1);

		allLayers = new FilteredLog();
		//AggregatedGrouped twice to avoid duplicate grouping
		allLayers.setLog(new AggregatedChildLog(new AggregatedGroupedLog(new AggregatedParentLog(
				Arrays.asList(new HostChildLog(new AggregatedGroupedLog(new HostParentLog(
						Arrays.asList(processed))), host1))))));
	}

	@Test
	public void testCountBase() throws Exception {
		Bench b = new Bench("unit");
		IEntry e = Entry.FIRST;
		int i = 0;
		while (true) {
			e = processed.getAt(e, 1);
			if (e == Entry.LAST) {
				assertEquals(i, sizeJournal.length);
				break;
			}
			assertEquals(timeJournal[i], e.getLogTime());
			assertEquals(sizeJournal[i], e.getChildren().size());

			i++;
		}
		b.stop();
		b.print(true);
	}

	@Test
	public void testCountAllLayers() throws Exception {
		Bench b = new Bench("unit");
		IEntry e = Entry.FIRST;
		int i = 0;
		while (true) {
			e = allLayers.getAt(e, 1);
			if (e == Entry.LAST) {
				assertEquals(i, sizeJournal.length);
				break;
			}
			assertEquals(timeJournal[i], e.getLogTime());
			assertEquals(sizeJournal[i], e.getChildren().size());

			i++;
		}
		b.stop();
		b.print(true);
	}


}
