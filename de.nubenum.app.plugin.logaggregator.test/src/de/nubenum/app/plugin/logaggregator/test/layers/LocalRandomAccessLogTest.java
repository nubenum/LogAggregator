package de.nubenum.app.plugin.logaggregator.test.layers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.layers.LocalRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;

public class LocalRandomAccessLogTest {
	public static final Path file = Paths.get("tmp/localrandom.log");

	@BeforeClass
	public static void setup() throws IOException {
		File d = new File("tmp");
		d.mkdir();
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file.toFile()), StandardCharsets.UTF_8));
		out.write("Lorem ipsum dolor sit amet\n");
		out.write("amet sit dolor ipsum Lorem\n");
		out.close();
	}

	@Test
	public void testInBounds() throws IOException, EndOfLogReachedException {
		LocalRandomAccessLog log = new LocalRandomAccessLog(file);
		assertThat(TestHelper.str(log.getAt(new FilePosition(0, 5), Direction.DOWN)), startsWith(" ipsum dolor sit ame"));
		assertThat(TestHelper.str(log.getAt(new FilePosition(0, 40), Direction.UP)), endsWith("amet sit dolor"));
		assertThat(TestHelper.str(log.getAt(new FilePosition(0, 40), Direction.DOWN)), startsWith("r ipsum Lorem\n"));
	}

	@Test
	public void testAtBounds() throws IOException, EndOfLogReachedException {
		LocalRandomAccessLog log = null;
		log = new LocalRandomAccessLog(file);
		assertEquals("r ipsum Lorem\n", TestHelper.str(log.getAt(new FilePosition(0, 40), Direction.DOWN)));
		assertEquals("Lorem ipsum", TestHelper.str(log.getAt(new FilePosition(0, 10), Direction.UP)));
		assertThat(TestHelper.str(log.getAt(new FilePosition(0, 0), Direction.DOWN)), startsWith("Lorem"));
		assertThat(TestHelper.str(log.getAt(new FilePosition(0, 53), Direction.UP)), endsWith("ipsum Lorem\n"));
	}

	@Test
	public void testOutBounds() {
		try {
			LocalRandomAccessLog log = new LocalRandomAccessLog(file);
			log.getAt(new FilePosition(0, -1), Direction.DOWN);
			fail("should throw exception");
		} catch (EndOfLogReachedException e) {
			assertEquals(Direction.UP, e.getDir());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		try {
			LocalRandomAccessLog log = new LocalRandomAccessLog(file);
			log.getAt(new FilePosition(0, 54), Direction.DOWN);
			fail("should throw exception");
		} catch (EndOfLogReachedException e) {
			assertEquals(Direction.DOWN, e.getDir());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetLength() {
		LocalRandomAccessLog log;
		try {
			log = new LocalRandomAccessLog(file);
			assertEquals(54, log.getLength());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
