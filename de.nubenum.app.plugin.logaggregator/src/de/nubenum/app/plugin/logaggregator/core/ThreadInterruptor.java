package de.nubenum.app.plugin.logaggregator.core;

import java.nio.channels.ClosedByInterruptException;

/**
 * A class that mimics the behavior of Thread.interrupt() and
 * Thread.interrupted(). Use this class instead of the Thread class to set and
 * check the interrupted flag to avoid blocking I/O operations to be interrupted
 * and the files closed on every interrupt (see
 * {@link ClosedByInterruptException}). Bear in mind that with this class, all
 * running threads checking this flag will interrupt themselves.
 *
 */
public class ThreadInterruptor {
	private static boolean interrupted = false;

	/**
	 * Request all threads to shutdown.
	 */
	public static void interrupt() {
		interrupted = true;
	}

	/**
	 * Reset the interrupt flag to false.
	 */
	public static void reset() {
		interrupted = false;
	}

	/**
	 * Check whether a shutdown was requested. Unlike {@link Thread#interrupted()}, the status is not reset (!).
	 *
	 * @return True when the thread should be interrupted, false otherwise
	 *
	 */
	public static boolean isInterrupted() {
		return interrupted;
	}
}
