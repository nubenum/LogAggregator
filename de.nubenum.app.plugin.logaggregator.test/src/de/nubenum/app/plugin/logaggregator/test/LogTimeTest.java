package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.model.LogTime;

public class LogTimeTest {

	@Test
	public void testAmericanSummer() {
		String line = "5/26/18 0:00:04:274 CEST";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2018, 5, 26, 0, 0, 4, 274);
		assertEquals(set, parsed);
	}

	@Test
	public void testAmericanWinter() {
		String line = "1/26/18 13:14:04:274 CET";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2018, 1, 26, 13, 14, 4, 274);
		assertEquals(set, parsed);
	}

	@Test
	public void testInverseInverse() {
		String line = "14-05-2018 05:06:13:055";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2018, 5, 14, 5, 6, 13, 55);
		assertEquals(set, parsed);
	}

	@Test
	public void testInverse() {
		String line = "2018-05-14 05:06:13:055";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2018, 5, 14, 5, 6, 13, 55);
		assertEquals(set, parsed);
	}

	@Test
	public void testInverseWithZone() {
		String line = "2016-02-29 00:00:00:000 CEST";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2016, 2, 29, 0, 0, 0, 0);
		assertEquals(set, parsed);
	}

	@Test
	public void testInverseWithoutMilli() {
		String line = "1997-02-28 15:06:09";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(1997, 2, 28, 15, 6, 9, 0);
		assertEquals(set, parsed);
	}


	@Test
	public void testInverseWithoutLeading() {
		String line = "2018-4-07 15:06:09";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2018, 4, 7, 15, 6, 9, 0);
		assertEquals(set, parsed);
	}

	@Test
	public void testInverseWithDot() {
		String line = "2016-02-29 00:00:00.050";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2016, 2, 29, 0, 0, 0, 50);
		assertEquals(set, parsed);
	}

	@Test
	public void testUnix() {
		String line = "1523052003756";
		LogTime parsed = new LogTime(line);
		LogTime set = new LogTime(2018, 4, 7, 0, 0, 3, 756);
		assertEquals(set, parsed);
	}

	@Test
	public void testCustom() {
		String line = "2018-05-14 05:06:13/055";
		LogTime parsed;
		try {
			parsed = new LogTime(line);
			fail("should throw exception");
		} catch (DateTimeParseException e) {
			assertNotNull(e.getMessage());
			LogTime.setCustomLogTimeFormats(Arrays.asList("yyyy-MM-dd HH:mm:ss/SSS"));
			parsed = new LogTime(line);
			LogTime set = new LogTime(2018, 5, 14, 5, 6, 13, 55);
			assertEquals(set, parsed);
		}
	}

}
