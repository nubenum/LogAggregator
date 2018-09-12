package de.nubenum.app.plugin.logaggregator.core.model;

/**
 * A class representing a link in a stacktrace.
 *
 */
public class EntryMessageLink extends EntryMessageMatch {
	private String linkedPackage;
	private String linkedClass;
	private String linkedMethod;
	private int linkedLine;

	/**
	 *
	 * @param start The char position of the start of the link (inclusive).
	 * @param end The char position of the end of the link (exclusive).
	 * @param linkedPackage The package of the linked class (e.g. com.example.app)
	 * @param linkedClass The name of the linked class (e.g. TestClass)
	 * @param linkedMethod The name of the linked Method (e.g. testMethod)
	 * @param linkedLine The number of the line (starting at 0)
	 */
	public EntryMessageLink(int start, int end, String linkedPackage, String linkedClass, String linkedMethod, int linkedLine) {
		super(start, end);
		this.linkedPackage = linkedPackage;
		this.linkedClass = linkedClass;
		this.linkedMethod = linkedMethod;
		this.linkedLine = linkedLine;
	}

	/**
	 * @return linkedPackage The package of the linked class (e.g. com.example.app)
	 */
	public String getLinkedPackage() {
		return linkedPackage;
	}

	/**
	 * @return linkedPackage The name of the linked class (e.g. TestClass)
	 */
	public String getLinkedClass() {
		return linkedClass;
	}

	/**
	 * @return linkedPackage The name of the linked Method (e.g. testMethod)
	 */
	public String getLinkedMethod() {
		return linkedMethod;
	}

	/**
	 * @return linkedPackage The number of the line (starting at 0)
	 */
	public int getLinkedLine() {
		return linkedLine;
	}
}
