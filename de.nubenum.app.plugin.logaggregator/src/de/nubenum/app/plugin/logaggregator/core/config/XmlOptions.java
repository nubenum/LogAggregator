package de.nubenum.app.plugin.logaggregator.core.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class XmlOptions implements IOptions {
	private Boolean enableMultithreading;
	private Boolean enableEntireFileCache;
	private Boolean enableFileWatcher;
	private Boolean enableAutoClose;
	private List<String> customLogTimeFormats = new ArrayList<>();

	@Override
	public Boolean getEnableMultithreading() {
		if (enableMultithreading == null)
			return true;
		return enableMultithreading;
	}

	@XmlAttribute
	@Override
	public void setEnableMultithreading(Boolean enableMultithreading) {
		this.enableMultithreading = enableMultithreading;
	}


	@Override
	public Boolean getEnableEntireFileCache() {
		if (enableEntireFileCache == null)
			return false;
		return enableEntireFileCache;
	}

	@XmlAttribute
	@Override
	public void setEnableEntireFileCache(Boolean enableEntireFileCache) {
		this.enableEntireFileCache = enableEntireFileCache;
	}

	@Override
	public Boolean getEnableFileWatcher() {
		if (enableFileWatcher == null)
			return true;
		return enableFileWatcher;
	}

	@XmlAttribute
	@Override
	public void setEnableFileWatcher(Boolean enableFileWatcher) {
		this.enableFileWatcher = enableFileWatcher;
	}

	@Override
	public Boolean getEnableAutoClose() {
		if (enableAutoClose == null)
			return true;
		return enableAutoClose;
	}

	@XmlAttribute
	@Override
	public void setEnableAutoClose(Boolean enableAutoClose) {
		this.enableAutoClose = enableAutoClose;
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
