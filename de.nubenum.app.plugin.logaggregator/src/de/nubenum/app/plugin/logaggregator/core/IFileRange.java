package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public interface IFileRange extends Comparable<IFileRange> {
	IFilePosition getTop();
	IFilePosition getBottom();
	IFilePosition getStart(Direction dir);
	IFileRange clip(long length);
	int getLength();
	boolean inRange(IFilePosition pos);
}
