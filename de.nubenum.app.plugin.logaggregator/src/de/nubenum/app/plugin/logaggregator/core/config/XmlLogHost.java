package de.nubenum.app.plugin.logaggregator.core.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import de.nubenum.app.plugin.logaggregator.core.Utils;

public class XmlLogHost implements ILogHost {

	private String name;
	private String shortName;

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
	public String getShortName() {
		if (shortName == null)
			return name;
		return shortName;
	}

	@Override
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	@Override
	public boolean equals(Object other) {
		return Utils.objectsEqual(XmlLogHost.class, this, other, h -> h.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
