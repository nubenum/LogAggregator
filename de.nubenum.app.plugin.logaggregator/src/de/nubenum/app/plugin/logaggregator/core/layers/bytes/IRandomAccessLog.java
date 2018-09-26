package de.nubenum.app.plugin.logaggregator.core.layers.bytes;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;

/**
 * A byte-based log that can be randomly accessed on byte-level
 *
 */
public interface IRandomAccessLog {
	/**
	 * Close this file. Please note that unlike traditional close operations, the
	 * file should be reopened internally if another read is executed.
	 */
	void close();

	/**
	 * Read bytes from the start in direction.
	 *
	 * @param start
	 *            The position where to start reading (inclusive)
	 * @param dir
	 *            The Direction in which to read
	 * @return A RandomByteBuffer with an arbitrary length, depending on the
	 *         remaining bytes available and internal buffer sizes
	 * @throws EndOfLogReachedException
	 *             If the end of the file is reached
	 * @throws IOException
	 *             If the backing storage is unavailable
	 */
	RandomByteBuffer getAt(IFilePosition start, Direction dir) throws EndOfLogReachedException, IOException;

	/**
	 * @return The total length of the file in bytes. This may be cached and not
	 *         up-to-date.
	 * @throws IOException
	 *             If the backing storage is unavailable
	 */
	long getLength() throws IOException;

	/**
	 * Get the total length of the file, optionally forcing a refresh of the length
	 *
	 * @param forceRefresh
	 *            If true, length will be reevaluated from disk instead of being
	 *            read from cache.
	 * @return The total length in bytes
	 * @throws IOException
	 *             If the backing storage is unavailable
	 */
	long getLength(boolean forceRefresh) throws IOException;
}
