package de.nubenum.app.plugin.logaggregator.config;

public interface ILogSource {
	String getName();
	void setName(String name);
	Boolean getIgnoreNotFound();
	void setIgnoreNotFound(Boolean ignoreFailure);
}
