package de.nubenum.app.plugin.logaggregator.core.config;

public interface IOptions {
	Boolean getEnableMultithreading();
	Boolean getEnableEntireFileCache();
	void setEnableMultithreading(Boolean enableMultithreading);
	void setEnableEntireFileCache(Boolean enableEntireFileCache);
}
