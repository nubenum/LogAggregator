package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;

public class HugeStackTraceTest {

	private static IChildLog processed;
	private static IFilteredLog allLayers;

	@BeforeClass
	public static void setup() throws IOException {
		ILogHost host1 = mock(ILogHost.class);
		when(host1.getName()).thenReturn("testdata/huge/");

		ILogSource src1 = mock(ILogSource.class);
		when(src1.getName()).thenReturn("SystemOut");
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
	public void testBase() throws IOException, InterruptedException {
		Bench b = new Bench("unit");
		IEntry e = processed.getAt(Entry.LAST, -1);
		b.stop();
		e = processed.getAt(new LinedEntry(new LogTime(2018,5,16,9,55,0,0)), 1);
		b.print(true);
		assertTrue(e.getLogTime().equals(new LogTime(2018,5,16,9,55,16,144)));
		assertTrue(e.getChildren().size() > 1000);
	}

	@Test
	public void testAllLayers() throws IOException, InterruptedException {
		Bench b = new Bench("unit");
		IEntry e = allLayers.getAt(Entry.LAST, -1);
		b.stop();
		e = allLayers.getAt(new LinedEntry(new LogTime(2018,5,16,9,55,0,0)), 1);
		b.print(true);
		assertTrue(e.getLogTime().equals(new LogTime(2018,5,16,9,55,16,144)));
		assertTrue(e.getChildren().size() > 1000);
	}

	@Test
	public void testCountBase() throws Exception {
		// bash script to generate reference file:
		// grep -ohE "^\[[0-9/]+/18 [0-9A-Z: ]+\]" $(echo $(ls SystemOut_*.log) SystemOut.log) > hugeReference.txt
		// the first entry gets LogTime.MIN and is not included in the reference file

		BufferedReader br = new BufferedReader(new FileReader("testdata/hugeReference.journal.log"));
		Bench b = new Bench("unit");
		IEntry e = Entry.FIRST;
		int i = 0;

		while (true) {
			e = processed.getAt(e, 1);
			if (e == Entry.LAST) {
				assertNull(br.readLine());
				break;
			}
			if (i != 0) {
				assertEquals(br.readLine(), getOrigTs(e.getActualLogTime() != null ? e.getActualLogTime() : e.getLogTime()));
			}
			i++;
		}

		b.stop();
		b.print(true);
		br.close();
		assertEquals(83734, i);
	}

	@Test
	public void testCountAllLayers() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("testdata/hugeReference.journal.log"));
		Bench b = new Bench("unit");
		IEntry e = Entry.FIRST;
		int i = 0;
		while (true) {
			e = allLayers.getAt(e, 1);
			if (e == Entry.LAST) {
				assertNull(br.readLine());
				break;
			}
			if (i != 0) {
				assertEquals(br.readLine(), getOrigTs(e.getActualLogTime() != null ? e.getActualLogTime() : e.getLogTime()));
			}
			i++;
		}
		b.stop();
		b.print(true);
		br.close();
		assertEquals(83734, i);
	}

	private String getOrigTs(LogTime t) throws Exception {
		Field f;
		f = t.getClass().getDeclaredField("time");
		f.setAccessible(true);
		LocalDateTime l = (LocalDateTime) f.get(t);
		return "["+l.format(DateTimeFormatter.ofPattern("M/d/yy H:mm:ss:SSS", Locale.US))+" CEST]";
	}


}
