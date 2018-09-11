package de.nubenum.app.plugin.logaggregator.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class XmlLogSource implements ILogSource {
	
	private String name;
	private Boolean ignoreNotFound;
	
	@XmlValue
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	@Override
	public Boolean getIgnoreNotFound() {
		if (ignoreNotFound == null)
			return false;
		return ignoreNotFound;
	}

	@Override
	public void setIgnoreNotFound(Boolean ignoreNotFound) {
		this.ignoreNotFound = ignoreNotFound;
	}

}
