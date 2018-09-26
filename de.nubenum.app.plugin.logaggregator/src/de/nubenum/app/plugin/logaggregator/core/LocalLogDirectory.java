package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;

/**
 * Implementation of ILogDirectory for directories on the local disk
 *
 */
public class LocalLogDirectory extends AbstractLogDirectory {

	public LocalLogDirectory(Path location, ILogHost host, ILogSource source) {
		super(location, host, source);
	}

	@Override
	protected List<Path> getAllFiles() throws IOException {
		File dir = path.toFile();
		File [] files = dir.listFiles();
		if (files == null || files.length == 0) throw new IOException(dir.getAbsolutePath() + " was not found.");
		return Arrays.stream(files).map(f -> f.toPath()).collect(Collectors.toList());
	}
}
