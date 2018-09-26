package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.IUpdateInitiator;
import de.nubenum.app.plugin.logaggregator.core.IUpdateListener;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
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
public class LocalRandomAccessLog implements IRandomAccessLog, IUpdateInitiator {
	private static final int MIN_BLOCK_SIZE = 8192;
	private static final int MAX_BLOCK_SIZE = 8192*8;
	private int blockSize = MIN_BLOCK_SIZE;

	private List<IUpdateListener> listeners = new ArrayList<>();

	private IFileRange lastRange = null;

	private Path path;
	private FileChannel file;
	private long length = -1;

	private RandomByteBuffer entireFileCache = null;
	private boolean enableEntireFileCache;

	public LocalRandomAccessLog(Path path, boolean enableEntireFileCache) {
		this.path = path;
		this.enableEntireFileCache = enableEntireFileCache;
	}

	public LocalRandomAccessLog(Path path) {
		this(path, false);
	}

	private void openFile() throws IOException {
		if (file == null) {
			this.file = FileChannel.open(path, StandardOpenOption.READ);
			if (length == -1) {
				int newLength = (int) getLength(true);
				listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.SIZE, newLength)));
				//SystemLog.log("Opening " + path.getFileName());
				if (enableEntireFileCache) {
					//TODO
					entireFileCache = new RandomByteBuffer(Files.readAllBytes(path));
				}
			} else {
				getLength(true);
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

		updateBlockSize(start, dir);
		lastRange = new FileRange(start, blockSize, dir).clip(fileLength);
		long top = lastRange.getTop().getByteOffset();
		int len = lastRange.getLength();

		if (entireFileCache != null) {
			//TODO without copy
			return new RandomByteBuffer(Arrays.copyOfRange(entireFileCache.getBytes(), (int) top, (int)top+len));
		}

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
	public void close() {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				SystemLog.log(e);
			}
		}
		file = null;
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

	@Override
	public void addListener(IUpdateListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IUpdateListener listener) {
		listeners.remove(listener);
	}
}
