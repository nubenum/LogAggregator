package de.nubenum.app.plugin.logaggregator.core.model;

public class EntryMessageLink extends EntryMessageMatch {
	private String linkedPackage;
	private String linkedClass;
	private String linkedMethod;
	private int linkedLine;

	public EntryMessageLink(int start, int end, String linkedPackage, String linkedClass, String linkedMethod, String linkedLine) {
		super(start, end);
		this.linkedPackage = linkedPackage;
		this.linkedClass = linkedClass;
		this.linkedMethod = linkedMethod;
		this.linkedLine = Integer.parseInt(linkedLine);
	}

	public String getLinkedPackage() {
		return linkedPackage;
	}

	public String getLinkedClass() {
		return linkedClass;
	}

	public String getLinkedMethod() {
		return linkedMethod;
	}

	public int getLinkedLine() {
		return linkedLine;
	}
}
