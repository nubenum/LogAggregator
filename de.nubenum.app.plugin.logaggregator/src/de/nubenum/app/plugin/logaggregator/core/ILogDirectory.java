package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogSource;

public interface ILogDirectory {
	List<File> getSourceFiles(ILogSource source) throws IOException;
	Path getPath();
}