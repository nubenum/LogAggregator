package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogSource;

/**
 * Representing a directory that can contain multiple log sources. Can be implemented for different storage backends.
 *
 */
public interface ILogDirectory {
	/**
	 * Returns all matching rotated log files for the specified source in this directory.
	 * @param source The source log file to be looked for in this directory
	 * @return A sorted List of matching files
	 * @throws IOException when the directory is unavailable or no files were found
	 */
	List<File> getSourceFiles(ILogSource source) throws IOException;

	/**
	 * Get the Path of this directory
	 * @return The Path
	 */
	Path getPath();
}