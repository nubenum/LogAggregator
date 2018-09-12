package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.Arrays;

/**
 * The log levels.
 *
 */
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

	/**
	 * Get the names of all Levels (useful for gui widgets).
	 * @return An array of names
	 */
	public static String[] stringValues() {
		return Arrays.stream(Level.values()).map(v -> v.name()).toArray(String[]::new);
	}

	/**
	 * Obtain the level for that index (useful for index-based gui widgets)
	 * @param index The index of the level.
	 * @return The Level for that index
	 */
	public static Level get(int index) {
		return Level.values()[index];
	}
}