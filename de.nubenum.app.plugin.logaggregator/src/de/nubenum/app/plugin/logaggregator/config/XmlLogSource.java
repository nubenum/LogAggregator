package de.nubenum.app.plugin.logaggregator.config;

import java.util.Objects;

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

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof XmlLogSource) {
			XmlLogSource host = (XmlLogSource) other;
			if (host.getName().equals(name))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
