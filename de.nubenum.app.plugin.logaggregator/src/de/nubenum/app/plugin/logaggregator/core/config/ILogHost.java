package de.nubenum.app.plugin.logaggregator.core.config;

/**
 * A host is a directory that in turn may contain multiple sources. Usually,
 * roughly the same sources are contained in each host directory. Implementing
 * classes should override the equals method so that entities with the same name
 * are equal.
 *
 */
public interface ILogHost {
	/**
	 * Get the name, i.e. the directory path of this host.
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
	 * Get the alias for the long directory name. If no short name was set, the full
	 * name should be returned.
	 *
	 * @return The alias.
	 */
	String getShortName();

	/**
	 * Set the alias for the long directory name.
	 *
	 * @param shortName
	 *            The alias.
	 */
	void setShortName(String shortName);
}
