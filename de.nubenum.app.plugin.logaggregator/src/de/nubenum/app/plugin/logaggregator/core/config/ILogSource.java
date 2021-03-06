package de.nubenum.app.plugin.logaggregator.core.config;

/**
 * A source is a single type of log file (e.g. error, access, SystemOut). A
 * single source may actually consist of multiple files if log rotation is used.
 * Implementing classes should override the equals method so that entities with
 * the same name are equal.
 *
 */
public interface ILogSource {
	/**
	 * Get the name, i.e. the directory path of this source.
	 *
	 * @return The directory name.
	 */
	String getName();

	/**
	 * Set the directory name.
	 *
	 * @param name
	 *            The directory name.
	 */
	void setName(String name);

	/**
	 * Whether an error should be thrown if the defined source file is not found.
	 * Sometimes certain sources will not be available on all hosts, in that case,
	 * this flag can be used. The default is false, i.e. implementing classes should
	 * return false if nothing was set.
	 *
	 * @return True if no error is to be thrown.
	 */
	Boolean getIgnoreNotFound();

	/**
	 * Set whether an error should be thrown if the defined source file is not
	 * found.
	 *
	 * @param ignoreFailure
	 */
	void setIgnoreNotFound(Boolean ignoreFailure);
}
