package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.layers.IRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.LocalRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.RotatedRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;

public class RotatedRandomAccessLogTest {
	private static final Path[] files = {
			Paths.get("tmp/rotaterandom1.log"),
			Paths.get("tmp/rotaterandom2.log"),
			Paths.get("tmp/rotaterandom3.log")
	};
	private static List<IRandomAccessLog> logs;

	@BeforeClass
	public static void setup() throws FileNotFoundException {
		PrintWriter out;
		out = new PrintWriter(files[0].toFile());
		out.print("Lorem0 ipsum dolor sit amet\n");
		out.print("amet sit dolor ipsum0 Lorem\n");
		out.close();
		out = new PrintWriter(files[1].toFile());
		out.print("Lorem1 ipsum dolor sit amet\n");
		out.close();
		out = new PrintWriter(files[2].toFile());
		out.print("Lorem2 ipsum dolor sit amet\n");
		out.print("amet sit dolor ipsum2 Lorem\n");
		out.close();

		logs = Arrays.stream(files)
				.map(p -> {
					return new LocalRandomAccessLog(p);
				})
				.collect(Collectors.toList());
	}

	@Test
	public void testInBounds() throws EndOfLogReachedException, IOException {
		RotatedRandomAccessLog log = new RotatedRandomAccessLog(logs);
		assertEquals("1 ipsum dolor sit amet\n", TestHelper.str(log.getAt(new FilePosition(1, 5), Direction.DOWN)));
		assertEquals("or ipsum0 Lorem\n", TestHelper.str(log.getAt(new FilePosition(0, 40), Direction.DOWN)));
		assertEquals("or ipsum0 L", TestHelper.str(log.getAt(new FilePosition(0, 50), Direction.UP), Direction.UP)); //cache bound
		assertEquals("Lorem0 ipsum dolor si", TestHelper.str(log.getAt(new FilePosition(0, 20), Direction.UP)));
		assertEquals("um dolor si", TestHelper.str(log.getAt(new FilePosition(0, 10), Direction.DOWN), Direction.DOWN)); //cache bound
		assertThat(TestHelper.str(log.getAt(new FilePosition(2, 5), Direction.DOWN)), startsWith("2 ipsum"));
		assertThat(TestHelper.str(log.getAt(new FilePosition(0, 84), Direction.DOWN)), startsWith("Lorem"));
	}

	@Test
	public void testOutBounds() {
		try {
			RotatedRandomAccessLog log = new RotatedRandomAccessLog(logs);
			log.getAt(new FilePosition(0, -30), Direction.DOWN);
			fail("should throw exception");
		} catch (EndOfLogReachedException e) {
			assertEquals(Direction.UP, e.getDir());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		try {
			RotatedRandomAccessLog log = new RotatedRandomAccessLog(logs);
			log.getAt(new FilePosition(0, 200), Direction.UP);
			fail("should throw exception");
		} catch (EndOfLogReachedException e) {
			assertEquals(Direction.DOWN, e.getDir());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetLength() {
		RotatedRandomAccessLog log = new RotatedRandomAccessLog(logs);
		assertEquals(140, log.getLength());
	}


}
