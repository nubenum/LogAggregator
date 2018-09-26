package de.nubenum.app.plugin.logaggregator.core.layers.bytes;

import java.io.IOException;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.IFileRange;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;

/**
 * An implementation for a file that is made up of multiple file parts that can be accessed continuously.
 *
 */
public class RotatedRandomAccessLog implements IRandomAccessLog {
	private List<IRandomAccessLog> fileParts;
	private RandomByteBuffer[] cache = new RandomByteBuffer[2];
	private long cachedLength = -1;

	public RotatedRandomAccessLog(List<IRandomAccessLog> fileParts) {
		this.fileParts = fileParts;
	}

	@Override
	public RandomByteBuffer getAt(IFilePosition start, Direction dir) throws IOException, EndOfLogReachedException {

		start = translateFirstLast(start);

		RandomByteBuffer cached = getCachedBytesAt(start, dir);
		if (cached != null)
			return cached;

		return getNewBytesAt(start, dir);
	}

	private RandomByteBuffer getNewBytesAt(IFilePosition start, Direction dir) throws EndOfLogReachedException, IOException {
		IRandomAccessLog part = fileParts.get(start.getPartOffset());

		while (start.getByteOffset() < 0) {
			if (start.getPartOffset()-1 < 0)
				throw new EndOfLogReachedException(Direction.UP);
			part = fileParts.get(start.getPartOffset()-1);
			start = new FilePosition(start.getPartOffset()-1, start.getByteOffset()+part.getLength());
		}

		while (start.getByteOffset() >= part.getLength()) {
			if (start.getPartOffset()+1 >= fileParts.size())
				throw new EndOfLogReachedException(Direction.DOWN);
			start = new FilePosition(start.getPartOffset()+1, start.getByteOffset()-part.getLength());
			part = fileParts.get(start.getPartOffset());
		}

		cache[1] = cache[0];
		cache[0] = getActualBytesOfPart(part, start, dir);
		return cache[0];
	}

	private IFilePosition translateFirstLast(IFilePosition start) throws IOException {
		if (start == FilePosition.FIRST) {
			start = new FilePosition(0, 0);
		} else if (start == FilePosition.LAST) {
			start = new FilePosition(fileParts.size()-1, fileParts.get(fileParts.size()-1).getLength()-1);
		}
		return start;
	}

	private RandomByteBuffer getCachedBytesAt(IFilePosition start, Direction dir) {
		for(int i=0;i<cache.length;i++) {
			if (cache[i] != null && cache[i].getRange().inRange(start)) {
				int bufferOffset = (int) (start.getByteOffset() - cache[i].getRange().getTop().getByteOffset());
				RandomByteBuffer buffer = new RandomByteBuffer(cache[i], bufferOffset, dir);
				return buffer;
			}
		}
		return null;
	}

	private RandomByteBuffer getActualBytesOfPart(IRandomAccessLog file, IFilePosition start, Direction dir)
			throws IOException, EndOfLogReachedException {

		RandomByteBuffer buffer = file.getAt(start, dir);
		IFileRange range = new FileRange(start, buffer.getOffsetLength(), dir);
		int offset = start.distance(range.getTop());
		return new RandomByteBuffer(buffer, range, offset, dir);
	}

	@Override
	public void close() {
		for (IRandomAccessLog file : fileParts) {
			file.close();
		}
	}

	@Override
	public long getLength() {
		return getLength(false);
	}

	@Override
	public long getLength(boolean forceRefresh) {
		if (cachedLength == -1 || forceRefresh)
			updateLength();
		return cachedLength;
	}

	private void updateLength() {
		this.cachedLength = fileParts.stream().mapToLong(f -> {
			try {
				return f.getLength(true);
			} catch (IOException e) {
				return 0;
			}
		}).sum();
	}
}
