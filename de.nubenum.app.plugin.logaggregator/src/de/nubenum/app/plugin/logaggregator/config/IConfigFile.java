package de.nubenum.app.plugin.logaggregator.config;

import java.io.File;
import java.io.IOException;

public interface IConfigFile {
	/**
	 * Set the config file.
	 * @param file The file
	 */
	void setFile(File file);
	IConfig getConfig();
	void read() throws IOException;
	void write() throws IOException;
}
