package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.config.IConfig;

public class ConfigProvider {
	private static IConfig config;

	public static void setConfig(IConfig config) {
		ConfigProvider.config = config;
	}
	public static IConfig getConfig() {
		return config;
	}

	public static boolean getEnableMultithreading() {
		if (config != null && config.getOptions() != null && config.getOptions().getEnableMultithreading() != null)
			return config.getOptions().getEnableMultithreading();
		return true;
	}

	public static boolean getEnableEntireFileCache() {
		if (config != null && config.getOptions() != null && config.getOptions().getEnableEntireFileCache() != null)
			return config.getOptions().getEnableEntireFileCache();
		return false;
	}
}
