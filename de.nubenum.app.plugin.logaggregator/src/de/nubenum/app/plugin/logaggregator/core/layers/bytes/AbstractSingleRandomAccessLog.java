package de.nubenum.app.plugin.logaggregator.core.layers.bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.IUpdateInitiator;
import de.nubenum.app.plugin.logaggregator.core.IUpdateListener;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.IFileRange;

/**
 * An abstract class providing tools needed for implementing a random access
 * file that represents a single file and can be concretized for different
 * storage backends.
 *
 */
public abstract class AbstractSingleRandomAccessLog implements IRandomAccessLog, IUpdateInitiator {

	private static final int MIN_BLOCK_SIZE = 8192;
	private static final int MAX_BLOCK_SIZE = 8192 * 8;
	protected int blockSize = MIN_BLOCK_SIZE;
	protected IFileRange lastRange = null;
	protected List<IUpdateListener> listeners = new ArrayList<>();
	protected boolean enableEntireFileCache;

	private IFilePosition translateFirstLast(IFilePosition start) throws IOException {
		if (start == FilePosition.FIRST) {
			start = new FilePosition(0, 0);
		} else if (start == FilePosition.LAST) {
			start = new FilePosition(0, getLength() - 1);
		}
		return start;
	}

	private void updateBlockSize(IFilePosition start, Direction dir) {
		if (lastRange != null && lastRange.getNext(dir).equals(start)) {
			// TODO performance analysis
			blockSize *= 2;
			if (blockSize > MAX_BLOCK_SIZE)
				blockSize = MAX_BLOCK_SIZE;
		} else {
			blockSize = MIN_BLOCK_SIZE;
		}
	}

	/**
	 * Calculate the actual range of bytes to be read, using the length of the file.
	 *
	 * @param start
	 *            The position where to start reading
	 * @param dir
	 *            The Direction in which to read
	 * @return The range of bytes to be read
	 * @throws IOException
	 *             If {@link #getLength()} fails
	 * @throws EndOfLogReachedException
	 *             If the range is completely outside the file boundaries
	 */
	protected IFileRange calculateRequestedRange(IFilePosition start, Direction dir)
			throws IOException, EndOfLogReachedException {
		long fileLength = getLength();
		if (start.getByteOffset() < 0)
			throw new EndOfLogReachedException(Direction.UP);
		if (start.getByteOffset() >= fileLength)
			throw new EndOfLogReachedException(Direction.DOWN);

		start = translateFirstLast(start);

		updateBlockSize(start, dir);
		return new FileRange(start, blockSize, dir).clip(fileLength);
	}

	@Override
	public long getLength() throws IOException {
		return getLength(false);
	}

	@Override
	public void addListener(IUpdateListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IUpdateListener listener) {
		listeners.remove(listener);
	}

}