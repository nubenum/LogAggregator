package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import jcifs.smb.SmbFile;

/**
 * Implementation of ILogDirectory for directories on the local disk
 *
 */
public class SmbLogDirectory extends AbstractLogDirectory {

	public SmbLogDirectory(Path location, ILogHost host, ILogSource source) {
		super(location, host, source);
	}

	@Override
	protected List<Path> getAllFiles() throws IOException {
		File dir = path.toFile();
		SmbFile baseDir = new SmbFile(path.toString());
		SmbFile[] files = baseDir.listFiles();
		if (files == null || files.length == 0) throw new IOException(dir.getAbsolutePath() + " was not found.");

		return Arrays.stream(files).map(f -> {
			try {
				return Paths.get(f.getURL().toURI());
			} catch (URISyntaxException e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}
}
