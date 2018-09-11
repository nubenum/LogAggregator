package de.nubenum.app.plugin.logaggregator.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

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
		if (this.shortName == null)
			this.shortName = name;
	}

	@XmlAttribute
	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

}
