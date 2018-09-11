package de.nubenum.app.plugin.logaggregator.config;

import java.io.File;
import java.io.IOException;

public interface IConfigFile {
	void setFile(File file);
	IConfig getConfig();
	void read() throws IOException;
	void write() throws IOException;
}
