package de.nubenum.app.plugin.logaggregator.core.model;

/**
 * A Direction within a log, i.e. up = older and down = newer.
 *
 */
public enum Direction {
	UP(-1),
	DOWN(1),
	NONE(0);

	private int value;

	private Direction(int value) {
		this.value = value;
	}

	/**
	 * An integer value representing this Direction.
	 * @return 1 for DOWN, -1 for UP, 0 for NONE.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get the Direction from a Long.
	 * @param offset Offset indicating the Direction.
	 * @return DOWN for offset > 0, UP for offset < 0, else NONE.
	 */
	public static Direction get(Long offset) {
		if (offset == 0) return NONE;
		if (offset > 0) return DOWN;
		return UP;
	}

	/**
	 * Get the Direction from a signed int. {@see #get(offset)}
	 */
	public static Direction get(int offset) {
		return get((long) offset);
	}

	/**
	 * Convert an Entry.FIRST or Entry.LAST to the respective Direction.
	 * @param entry An arbitrary IEntry
	 * @return DOWN if LAST, UP if FIRST, else NONE.
	 */
	public static Direction get(IEntry entry) {
		if (entry == Entry.FIRST) return UP;
		if (entry == Entry.LAST) return DOWN;
		return NONE;
	}

	/**
	 * Get the opposite Direction
	 * @return UP for DOWN, DOWN for UP, else NONE
	 */
	public Direction opposite() {
		if (this == UP)
			return DOWN;
		if (this == DOWN)
			return UP;
		return NONE;
	}
}