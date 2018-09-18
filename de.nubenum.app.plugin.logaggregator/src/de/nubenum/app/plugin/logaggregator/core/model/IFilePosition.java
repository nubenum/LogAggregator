package de.nubenum.app.plugin.logaggregator.core.model;

/**
 * Represents a position in a multi-part file, e.g. a continuous file that is
 * spread over multiple actual files on disk. This is immutable.
 *
 */
public interface IFilePosition extends Comparable<IFilePosition> {
	/**
	 * Obtain a new IFilePosition that has the given offset in the given direction
	 * from this position.
	 *
	 * @param offset
	 *            The distance from this position
	 * @param dir
	 *            The Direction from this position
	 * @return The new IFilePosition
	 */
	IFilePosition offset(int offset, Direction dir);

	/**
	 * Obtain a new IFilePosition that has the given offset from this position.
	 *
	 * @param offset
	 *            The offset (positive or negative) from this position
	 * @return The new IFilePosition
	 */
	IFilePosition offset(int offset);

	/**
	 * @return The byte position in the file part
	 */
	long getByteOffset();

	/**
	 * @return The number of the file part
	 */
	int getPartOffset();

	/**
	 * @return Whether this this the topmost possible IFilePosition
	 */
	boolean isTopmost();

	/**
	 * The distance to another IFilePosition. This will throw an exception if the
	 * IFilePositions are in different file parts since it is not possible to
	 * calculate the distance between file parts.
	 *
	 * @param other The other IFilePosition
	 * @return the distance in bytes
	 */
	int distance(IFilePosition other);
}
