package de.nubenum.app.plugin.logaggregator.config;

import java.util.List;

/**
 * Representing a configuration containing the location, hosts, sources.
 *
 */
public interface IConfig {
	/**
	 * Get all hosts.
	 *
	 * @return The List of Hosts.
	 */
	List<? extends ILogHost> getHosts();

	/**
	 * Set the hosts.
	 *
	 * @param hosts
	 *            The List of hosts.
	 */
	void setHosts(List<? extends ILogHost> hosts);

	/**
	 * Get all sources.
	 *
	 * @return
	 */
	List<? extends ILogSource> getSources();

	/**
	 * Set the sources.
	 *
	 * @param files
	 *            The List of sources.
	 */
	void setSources(List<? extends ILogSource> files);

	/**
	 * Get the location where the host directories are to be found.
	 *
	 * @return The location
	 */
	String getLocation();

	/**
	 * Set the location.
	 *
	 * @param location
	 *            The location.
	 */
	void setLocation(String location);

	IOptions getOptions();

	void setOptions(IOptions options);
}