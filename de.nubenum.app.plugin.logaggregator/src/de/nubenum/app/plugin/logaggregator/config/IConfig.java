package de.nubenum.app.plugin.logaggregator.config;

import java.util.List;

public interface IConfig {
	List<? extends ILogHost> getHosts();
	void setHosts(List<? extends ILogHost> hosts);
	
	List<? extends ILogSource> getSources();
	void setSources(List<? extends ILogSource> files);
	
	String getLocation();
	void setLocation(String location);
}