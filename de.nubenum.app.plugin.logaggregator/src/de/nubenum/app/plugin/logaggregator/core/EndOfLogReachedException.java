package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

/**
 * Exception thrown by the RandomAccessLog layers if the end of the log is reached.
 *
 */
public class EndOfLogReachedException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Direction dir;

	/**
	 * Init.
	 * @param dir The Direction in which the end was encountered.
	 */
	public EndOfLogReachedException(Direction dir) {
		this.dir = dir;
	}

	/**
	 * Get the Direction in which the end was encountered.
	 * @return The Direction.
	 */
	public Direction getDir() {
		return dir;
	}

	/**
	 * @return Whether this is the end in Direction.UP
	 */
	public boolean isTop() {
		return dir == Direction.UP;
	}

	@Override
	public String getMessage() {
		return "Reached end in Direction "+getDir();
	}
}
