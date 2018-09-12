package de.nubenum.app.plugin.logaggregator.config;

public interface ILogSource {
	/**
	 * Get the name, i.e. the directory path of this source.
	 * @return The directory name.
	 */
	String getName();

	/**
	 * Set the directory name.
	 * @param name The directory name.
	 */
	void setName(String name);

	/**
	 * Whether an error should be thrown if the defined source file is not found. Sometimes certain sources will not be available on all hosts, in that case, this flag can be used.
	 * @return True if no error is to be thrown.
	 */
	Boolean getIgnoreNotFound();

	/**
	 * Set whether an error should be thrown if the defined source file is not found.
	 * @param ignoreFailure
	 */
	void setIgnoreNotFound(Boolean ignoreFailure);
}
