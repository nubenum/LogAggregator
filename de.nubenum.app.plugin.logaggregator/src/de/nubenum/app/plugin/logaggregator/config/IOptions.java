package de.nubenum.app.plugin.logaggregator.config;

public interface IOptions {
	Boolean getEnableMultithreading();
	Boolean getEnableEntireFileCache();
	void setEnableMultithreading(Boolean enableMultithreading);
	void setEnableEntireFileCache(Boolean enableEntireFileCache);
}
