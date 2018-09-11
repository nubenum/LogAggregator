package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

/**
 * Represents a position in a multi-part file, e.g. a continuous file that is
 * spread over multiple actual files on disk. This is immutable.
 *
 */
public interface IFilePosition extends Comparable<IFilePosition> {
	/**
	 * Obtain a new IFilePosition that has the given offset in the given direction from this position.
	 * @param offset The
	 * @param dir
	 * @return
	 */
	IFilePosition offset(int offset, Direction dir);

	IFilePosition offset(int offset);

	long getByteOffset();

	int getPartOffset();

	boolean isTopmost();

	int distance(IFilePosition top);
}
