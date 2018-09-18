package de.nubenum.app.plugin.logaggregator.core.config;

import javax.xml.bind.annotation.XmlAttribute;

public class XmlOptions implements IOptions {
	private Boolean enableMultithreading;
	private Boolean enableEntireFileCache;

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
}
