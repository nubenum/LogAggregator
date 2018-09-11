package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.Arrays;

public enum Level {
	FATAL,
	SEVERE,
	ERROR,
	WARN,
	WARNING,
	OTHER,
	AUDIT,
	INFO,
	CONFIG,
	DEBUG,
	TRACE,
	DETAIL,
	EVENT,
	FINE,
	FINER,
	FINEST,
	ALL;

	public static String[] stringValues() {
		return Arrays.stream(Level.values()).map(v -> v.name()).toArray(String[]::new);
	}

	public static Level get(int index) {
		return Level.values()[index];
	}
}