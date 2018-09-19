package de.nubenum.app.plugin.logaggregator.core.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "aggregatorConfig")
public class XmlConfig implements IConfig {

	private List<? extends ILogHost> hosts = new ArrayList<>();
	private List<? extends ILogSource> sources = new ArrayList<>();
	private IOptions options;
	private String location;

	@Override
	public List<? extends ILogHost> getHosts() {
		return hosts;
	}

	@XmlElementWrapper(name = "hosts")
	@XmlElement(name = "host", type = XmlLogHost.class)
	@Override
	public void setHosts(List<? extends ILogHost> hosts) {
		this.hosts = hosts;
	}

	@XmlElementWrapper(name = "sources")
	@XmlElement(name = "source", type = XmlLogSource.class)
	@Override
	public List<? extends ILogSource> getSources() {
		return sources;
	}

	@Override
	public void setSources(List<? extends ILogSource> sources) {
		this.sources = sources;
	}

	@XmlElement(name = "options", type = XmlOptions.class)
	@Override
	public IOptions getOptions() {
		return options;
	}

	@Override
	public void setOptions(IOptions options) {
		this.options = options;
	}

	@Override
	@XmlElement(name = "location")
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}
}
