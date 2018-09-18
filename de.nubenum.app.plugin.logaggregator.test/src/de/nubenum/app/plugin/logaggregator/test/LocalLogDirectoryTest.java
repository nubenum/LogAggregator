package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.LocalLogDirectory;
import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;

public class LocalLogDirectoryTest {
	private static ILogHost host;
	private static ILogSource source;

	@BeforeClass
	public static void setup() throws IOException {
		File d = new File("tmp/host/sub");
		d.mkdirs();
		File f = new File("tmp/host/source.log");
		f.createNewFile();
		f = new File("tmp/host/source.log.2");
		f.createNewFile();
		f = new File("tmp/host/source.log.10");
		f.createNewFile();
		f = new File("tmp/host/source.log.bak");
		f.createNewFile();
		f = new File("tmp/host/source_test.log");
		f.createNewFile();
		f = new File("tmp/host/sub/subsource.1.log");
		f.createNewFile();

		host = new ILogHost() {

			@Override
			public void setShortName(String shortName) {
				return;
			}

			@Override
			public void setName(String name) {
				return;
			}

			@Override
			public String getShortName() {
				return null;
			}

			@Override
			public String getName() {
				return "host";
			}
		};
		source = new ILogSource() {
			private boolean ignore = false;
			private String name;
			@Override
			public void setName(String name) {
				this.name = name;
			}

			@Override
			public void setIgnoreNotFound(Boolean ignoreFailure) {
				this.ignore = ignoreFailure;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public Boolean getIgnoreNotFound() {
				return ignore;
			}
		};
	}

	@Test
	public void testRoot() throws IOException {
		source.setName("source.");
		LocalLogDirectory dir = new LocalLogDirectory(Paths.get("tmp/"), host, source);
		List<File> list = dir.getSourceFiles(source);
		assertEquals(4, list.size());
		assertEquals("source.log.2", list.get(0).getName());
		assertEquals("source.log.10", list.get(1).getName());
		assertEquals("source.log.bak", list.get(2).getName());
		assertEquals("source.log", list.get(3).getName());
	}

	@Test
	public void testSub() throws IOException {
		source.setName("sub/subsource");
		LocalLogDirectory dir = new LocalLogDirectory(Paths.get("tmp/"), host, source);
		List<File> list = dir.getSourceFiles(source);
		assertEquals(1, list.size());
	}

}
