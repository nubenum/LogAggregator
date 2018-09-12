package de.nubenum.app.plugin.logaggregator.core.model;

/**
 * A class representing a match in a message spanning over a certain range.
 *
 */
public class EntryMessageMatch {
	private int start;
	private int end;

	/**
	 *
	 * @param start The char position of the start of the match (inclusive).
	 * @param end The char position of the end of the match (exclusive).
	 */
	public EntryMessageMatch(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * @return The char position of the start of the match (inclusive).
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return The char position of the end of the match (exclusive).
	 */
	public int getEnd() {
		return end;
	}
}
