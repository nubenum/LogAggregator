package de.nubenum.app.plugin.logaggregator.core.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class XmlOptions implements IOptions {
	private Boolean enableMultithreading;
	private Boolean enableEntireFileCache;
	private List<String> customLogTimeFormats = new ArrayList<>();

	@XmlAttribute
	@Override
	public Boolean getEnableMultithreading() {
		if (enableMultithreading == null)
			return true;
		return enableMultithreading;
	}

	@Override
	public void setEnableMultithreading(Boolean enableMultithreading) {
		this.enableMultithreading = enableMultithreading;
	}

	@XmlAttribute
	@Override
	public Boolean getEnableEntireFileCache() {
		if (enableEntireFileCache == null)
			return false;
		return enableEntireFileCache;
	}

	@Override
	public void setEnableEntireFileCache(Boolean enableEntireFileCache) {
		this.enableEntireFileCache = enableEntireFileCache;
	}

	@XmlElementWrapper(name = "customLogTimeFormats")
	@XmlElement(name = "format")
	@Override
	public List<String> getCustomLogTimeFormats() {
		return customLogTimeFormats;
	}

	@Override
	public void setCustomLogTimeFormats(List<String> customLogTimeFormats) {
		this.customLogTimeFormats = customLogTimeFormats;
	}
}