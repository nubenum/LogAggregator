package de.nubenum.app.plugin.logaggregator.core.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import de.nubenum.app.plugin.logaggregator.core.EqualsHelper;

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
		return EqualsHelper.objectsEqual(XmlLogSource.class, this, other, h -> h.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
