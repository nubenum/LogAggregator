package de.nubenum.app.plugin.logaggregator.core.layers.dirs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;

/**
 * Representing a directory that can contain multiple log sources. Can be
 * implemented for different storage backends. Implementing classes should
 * override the equals method so that entities with the same path are equal.
 *
 */
public interface ILogDirectory {
	/**
	 * Returns all matching rotated log files for the specified source in this
	 * directory.
	 *
	 * @param source
	 *            The source log file to be looked for in this directory
	 * @return A sorted List of matching files
	 * @throws IOException
	 *             when the directory is unavailable or no files were found
	 */
	List<URI> getSourceFiles(ILogSource source) throws IOException;

	/**
	 * Get the Path of this directory
	 *
	 * @return The Path
	 */
	Path getPath();
}