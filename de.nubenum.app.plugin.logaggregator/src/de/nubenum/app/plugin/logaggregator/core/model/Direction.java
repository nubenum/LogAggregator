package de.nubenum.app.plugin.logaggregator.core.model;

public enum Direction {
	UP(-1),
	DOWN(1),
	NONE(0);

	private int value;

	private Direction(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Direction get(Long offset) {
		if (offset == 0) return NONE;
		if (offset > 0) return DOWN;
		return UP;
	}

	public static Direction get(int offset) {
		return get((long) offset);
	}

	public static Direction get(IEntry entry) {
		if (entry == Entry.FIRST) return UP;
		if (entry == Entry.LAST) return DOWN;
		return NONE;
	}

	public Direction opposite() {
		if (this == UP)
			return DOWN;
		if (this == DOWN)
			return UP;
		return NONE;
	}
}