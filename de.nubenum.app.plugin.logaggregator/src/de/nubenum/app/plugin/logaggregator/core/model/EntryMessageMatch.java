package de.nubenum.app.plugin.logaggregator.core.model;

public class EntryMessageMatch {
	private int start;
	private int end;

	public EntryMessageMatch(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}
