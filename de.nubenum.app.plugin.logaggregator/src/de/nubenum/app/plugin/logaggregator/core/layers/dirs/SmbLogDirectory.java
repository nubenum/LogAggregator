package de.nubenum.app.plugin.logaggregator.core.layers.dirs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.Utils;
import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import jcifs.smb.SmbFile;

/**
 * Implementation of ILogDirectory for directories on an smb drive
 *
 */
public class SmbLogDirectory extends AbstractLogDirectory {
	private URI path;

	public SmbLogDirectory(String location, ILogHost host, ILogSource source) throws IOException {
		try {
			this.path = new URI(location).resolve(ensureTrailingSlash(host.getName()));
			Path sourceParent = Paths.get(source.getName()).getParent();
			if (sourceParent != null)
				this.path = this.path.resolve(ensureTrailingSlash(sourceParent.toString()));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	private static String ensureTrailingSlash(String path) {
		return path + (path.endsWith("/") ? "" : "/");
	}

	@Override
	protected List<URI> getAllFiles() throws IOException {
		SmbFile baseDir = new SmbFile(path.toString());
		SmbFile[] files = baseDir.listFiles();
		if (files == null || files.length == 0) throw new FileNotFoundException(baseDir.getPath() + " was not found.");

		return Arrays.stream(files).map(f -> {
			try {
				return f.getURL().toURI();
			} catch (URISyntaxException e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public Path getPath() {
		return Paths.get(path.getPath());
	}

	@Override
	public boolean equals(Object other) {
		return Utils.objectsEqual(SmbLogDirectory.class, this, other, d -> d.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}
