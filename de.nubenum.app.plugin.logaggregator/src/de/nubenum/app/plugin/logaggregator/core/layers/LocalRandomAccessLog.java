package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.nubenum.app.plugin.logaggregator.core.ConfigProvider;
import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.IFileRange;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;

/**
 * An implementation for a single file that can be accessed randomly by byte.
 *
 */
public class LocalRandomAccessLog implements IRandomAccessLog {
	private static final int MIN_BLOCK_SIZE = 8192;
	private static final int MAX_BLOCK_SIZE = 8192*8;
	private int blockSize = MIN_BLOCK_SIZE;

	private IFileRange lastRange = null;

	private Path path;
	private FileChannel file;
	private long length = -1;

	private RandomByteBuffer entireFileCache = null;

	public LocalRandomAccessLog(Path path) {
		this.path = path;
	}

	private void openFile() throws IOException {
		if (file == null) {
			this.file = FileChannel.open(path, StandardOpenOption.READ);
			if (ConfigProvider.getEnableEntireFileCache()) {
				//TODO
				SystemLog.log("Actual entire file access " + path.getFileName());
				entireFileCache = new RandomByteBuffer(Files.readAllBytes(path));
			}
		}
	}

	@Override
	public RandomByteBuffer getAt(IFilePosition start, Direction dir) throws IOException, EndOfLogReachedException {
		long fileLength = getLength();
		if (start.getByteOffset() < 0)
			throw new EndOfLogReachedException(Direction.UP);
		if (start.getByteOffset() >= fileLength)
			throw new EndOfLogReachedException(Direction.DOWN);

		start = translateFirstLast(start);

		if (entireFileCache != null) {
			return new RandomByteBuffer(entireFileCache, (int) start.getByteOffset(), dir);
		}

		updateBlockSize(start, dir);
		lastRange = new FileRange(start, blockSize, dir).clip(fileLength);
		long top = lastRange.getTop().getByteOffset();
		int len = lastRange.getLength();

		openFile();
		ByteBuffer buf = ByteBuffer.allocate(len);
		file.position(top);
		int bytesRead = file.read(buf);
		assert(bytesRead == len);
		return new RandomByteBuffer(buf.array());
	}

	private IFilePosition translateFirstLast(IFilePosition start) throws IOException {
		if (start == FilePosition.FIRST) {
			start = new FilePosition(0, 0);
		} else if (start == FilePosition.LAST) {
			start = new FilePosition(0, getLength()-1);
		}
		return start;
	}

	private void updateBlockSize(IFilePosition start, Direction dir) {
		if (lastRange != null && lastRange.getNext(dir).equals(start)) {
			//TODO performance analysis
			blockSize *= 2;
			if (blockSize > MAX_BLOCK_SIZE)
				blockSize = MAX_BLOCK_SIZE;
		} else {
			blockSize = MIN_BLOCK_SIZE;
		}
	}

	@Override
	public void close() throws IOException {
		if (file != null)
			file.close();
	}

	@Override
	public long getLength() throws IOException {
		return getLength(false);
	}

	@Override
	public long getLength(boolean forceRefresh) throws IOException {
		if (length == -1 || forceRefresh) {
			openFile();
			length = file.size();
		}
		return length;
	}

	@Override
	public String toString() {
		return path.getFileName().toString();
	}
}
