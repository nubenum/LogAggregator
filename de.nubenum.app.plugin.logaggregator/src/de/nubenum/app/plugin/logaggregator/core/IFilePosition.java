package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public interface IFilePosition extends Comparable<IFilePosition> {
	IFilePosition offset(int offset, Direction dir);
	IFilePosition offset(int offset);
	long getByteOffset();
	int getPartOffset();
	boolean isTopmost();
	int distance(IFilePosition top);
}
