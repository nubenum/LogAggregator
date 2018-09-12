package de.nubenum.app.plugin.logaggregator.config;

import java.util.List;

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
	 * Get all sources. A source is a single type of log file (e.g. error, access,
	 * SystemOut). A single source may actually consist of multiple files if log
	 * rotation is used.
	 *
	 * @return
	 */
	List<? extends ILogSource> getSources();

	/**
	 * Set the sources.
	 * @param files The List of sources.
	 */
	void setSources(List<? extends ILogSource> files);

	/**
	 * Get the location where the host directories are to be found.
	 * @return The location
	 */
	String getLocation();

	/**
	 * Set the location.
	 * @param location The location.
	 */
	void setLocation(String location);
}