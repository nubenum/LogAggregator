package de.nubenum.app.plugin.logaggregator.core.model;

/**
 * A range of bytes in a multi-part file, defined by to IFilePositions. This is
 * immutable.
 *
 */
public interface IFileRange extends Comparable<IFileRange> {
	/**
	 * Get the top bound of this range (inclusive)
	 *
	 * @return The top IFilePosition
	 */
	IFilePosition getTop();

	/**
	 * Get the bottom bound of this range (exclusive)
	 *
	 * @return The bottom IFilePosition
	 */
	IFilePosition getBottom();

	/**
	 * Get the next adjacent IFilePosition outside this range
	 *
	 * @param dir
	 *            The Direction in which to get the next IFilePosition
	 * @return top-1 for Direction.UP, bottom for Direction.DOWN
	 */
	IFilePosition getNext(Direction dir);

	/**
	 * Clip this IFileRange with the beginning of the file part and the given length
	 * of this file part
	 *
	 * @param length
	 *            The length of this file part
	 * @return A new IFileRange with the same part id and top and bottom clipped to
	 *         0 and length respectively
	 */
	IFileRange clip(long length);

	int getLength();

	boolean inRange(IFilePosition pos);
}
