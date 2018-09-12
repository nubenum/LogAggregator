package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.Activator;

/**
 * Abstracting the log facilities of the used platform. This is for internal
 * logging and warnings for the LogAggregator app itself.
 *
 */
public class SystemLog {
	public static void log(String msg) {
		Activator.log(msg);
	}

	public static void log(Throwable exc) {
		Activator.log(exc);
	}

	public static void warn(String msg) {
		Activator.warn(msg);
	}
}
