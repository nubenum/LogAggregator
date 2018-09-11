package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public class EndOfLogReachedException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Direction dir;

	public EndOfLogReachedException(Direction dir) {
		this.dir = dir;
	}

	public Direction getDir() {
		return dir;
	}

	public boolean isTop() {
		return dir == Direction.UP;
	}

	@Override
	public String getMessage() {
		return "Reached end in Direction "+getDir();
	}
}
