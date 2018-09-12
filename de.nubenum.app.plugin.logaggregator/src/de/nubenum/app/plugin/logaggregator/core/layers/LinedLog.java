package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.FilePosition;
import de.nubenum.app.plugin.logaggregator.core.FileRange;
import de.nubenum.app.plugin.logaggregator.core.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;
import de.nubenum.app.plugin.logaggregator.core.IUpdateInitiator;
import de.nubenum.app.plugin.logaggregator.core.IUpdateListener;
import de.nubenum.app.plugin.logaggregator.core.RandomByteBuffer;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;

/**
 * An entry-based log backed by a byte-based log that will provide single lines
 * as entries from the byte stream. From the given reference in the given
 * direction it will read from the backing byte-based log until a complete line
 * was read. It will instantiate a new LinedEntry object and try to fix missing
 * timestamps. It will notify listeners periodically about the number of lines
 * read.
 *
 */
public class LinedLog implements IEntryLog, IUpdateInitiator {
	private static final byte LINE_SEPARATOR = '\n';
	private int avgLineSize = 100;
	private long readLines = 0;
	private IRandomAccessLog file;
	private ILogHost host;
	private ILogSource source;
	private List<IUpdateListener> listeners = new ArrayList<>();

	/**
	 * Init.
	 *
	 * @param file
	 *            The byte-based file this log will work on.
	 * @param host
	 *            The identifying host of this file.
	 * @param source
	 *            The identifying source of this file.
	 */
	public LinedLog(IRandomAccessLog file, ILogHost host, ILogSource source) {
		this.file = file;
		this.host = host;
		this.source = source;
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException {
		if (offset == 0)
			return reference;
		RandomByteBuffer buffer = new RandomByteBuffer();
		LinedEntry line;
		Direction endReached = null;
		Direction dir = Direction.get(offset);
		IFilePosition start = getStartPosition(reference, offset);
		do {
			try {
				RandomByteBuffer newRange = file.getAt(start, dir);
				buffer.concatOrdered(newRange);
			} catch (EndOfLogReachedException e) {
				if (buffer.getBytes() != null && buffer.getOffsetLength() > 0)
					endReached = e.getDir();
				else
					return Entry.getFirstOrLast(e.getDir());
			}
			start = buffer.getRange().getNext(dir);
			line = getCompleteEntry(buffer, dir, endDirection(reference, offset, endReached));
		} while (line == null && endReached == null);

		if (line == null)
			return Entry.getFirstOrLast(offset > 0 ? Direction.DOWN : Direction.UP);

		countReadLines();

		IEntry entry = fixMessedUpTimestamps(reference, dir, line);
		return entry;
	}

	private void countReadLines() {
		readLines++;
		if (readLines % 100000 == 0) {
			listeners.forEach(l -> l.onUpdate(new UpdateEvent(100000)));
		}
	}

	private Direction endDirection(IEntry reference, int offset, Direction endReached) {
		if (Math.abs(offset) <= 1) {
			if (reference == Entry.FIRST)
				return Direction.UP;
			if (reference == Entry.LAST)
				return Direction.DOWN;
		}
		if (endReached != null)
			return endReached;
		return null;
	}

	private IFilePosition getStartPosition(IEntry reference, int offset) throws IOException {
		if (reference == Entry.FIRST)
			return FilePosition.FIRST;
		if (reference == Entry.LAST)
			return FilePosition.LAST;

		IFilePosition start = reference.getRange().getNext(Direction.get(offset));
		if (Math.abs(offset) > 1) {
			start = start.offset(offset * avgLineSize);
		}
		return start;
	}

	private LinedEntry getCompleteEntry(RandomByteBuffer bytes, Direction dir, Direction endReached) {
		int[] lineRange = null;
		if (dir == Direction.DOWN)
			lineRange = findLineDown(bytes.getBytes(), bytes.getOffset(), endReached);
		else
			lineRange = findLineUp(bytes.getBytes(), bytes.getOffset(), endReached);

		if (lineRange != null) {
			int length = lineRange[1] - lineRange[0];
			IFileRange range = new FileRange(bytes.getRange().getTop().offset(lineRange[0]), length);
			String line = new String(bytes.getBytes(), lineRange[0], length, StandardCharsets.UTF_8);
			return new LinedEntry(line, range, host, source);
		}
		return null;
	}

	private LinedEntry fixMessedUpTimestamps(IEntry reference, Direction dir, LinedEntry entry) {
		if (entry.getRange().getTop().isTopmost() && entry.getLogTime() == null) {
			entry.setLogTime(LogTime.MIN);
			System.out.println("Header Timestamp spoofed: " + entry);
		}
		return entry;
	}

	private int[] findLineDown(byte[] bytes, int offset, Direction endReached) {
		Integer top = null;
		if (endReached == Direction.UP)
			top = 0;

		for (int i = offset; i < bytes.length; i++) {
			if (bytes[i] == LINE_SEPARATOR) {
				if (top == null)
					top = i + 1;
				else
					return new int[] { top, i };
			}
		}
		return null;
	}

	private int[] findLineUp(byte[] bytes, int offset, Direction endReached) {
		Integer bottom = null;

		for (int i = offset; i >= 0; i--) {
			if (bytes[i] == LINE_SEPARATOR) {
				if (bottom == null)
					bottom = i;
				else
					return new int[] { i + 1, bottom };
			}
		}
		if (endReached == Direction.UP && bottom != null)
			return new int[] { 0, bottom };
		return null;
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
