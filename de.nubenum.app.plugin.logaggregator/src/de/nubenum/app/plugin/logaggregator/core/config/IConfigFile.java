package de.nubenum.app.plugin.logaggregator.core.config;

import java.io.File;
import java.io.IOException;

/**
 * Representing a file that holds a configuration.
 *
 */
public interface IConfigFile {
	/**
	 * Set the config file to use to read and write.
	 *
	 * @param file
	 *            The file
	 */
	void setFile(File file);

	/**
	 * @return Get the config after it was read or set.
	 */
	IConfig getConfig();

	/**
	 * Set the config to be written.
	 *
	 * @param config
	 *            The config
	 */
	void setConfig(IConfig config);

	/**
	 * Read the config from the set file.
	 *
	 * @throws IOException
	 */
	void read() throws IOException;

	/**
	 * Write the config to the set file.
	 *
	 * @throws IOException
	 */
	void write() throws IOException;
}
