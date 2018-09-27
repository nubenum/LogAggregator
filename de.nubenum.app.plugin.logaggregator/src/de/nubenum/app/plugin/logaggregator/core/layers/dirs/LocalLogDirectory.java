package de.nubenum.app.plugin.logaggregator.core.layers.dirs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.Utils;
import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;

/**
 * Implementation of ILogDirectory for directories on the local disk
 *
 */
public class LocalLogDirectory extends AbstractLogDirectory {
	private Path path;

	public LocalLogDirectory(String location, ILogHost host, ILogSource source) {
		this.path = Paths.get(location).resolve(host.getName());
		Path sourceParent = Paths.get(source.getName()).getParent();
		if (sourceParent != null)
			this.path = this.path.resolve(sourceParent);
	}

	@Override
	protected List<URI> getAllFiles() throws IOException {
		File dir = path.toFile();
		File [] files = dir.listFiles();
		if (files == null || files.length == 0) throw new FileNotFoundException(dir.getAbsolutePath() + " was not found.");
		return Arrays.stream(files).map(f -> f.toURI()).collect(Collectors.toList());
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public boolean equals(Object other) {
		return Utils.objectsEqual(LocalLogDirectory.class, this, other, d -> d.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}
