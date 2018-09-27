package de.nubenum.app.plugin.logaggregator.core.layers.bytes;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;

/**
 * An implementation for a single file that can be accessed randomly by byte.
 *
 */
public class LocalRandomAccessLog extends AbstractSingleRandomAccessLog {
	private Path path;
	private FileChannel file;
	private long length = -1;

	private RandomByteBuffer entireFileCache = null;
	public LocalRandomAccessLog(URI path, boolean enableEntireFileCache) {
		this.path = Paths.get(path);
		this.enableEntireFileCache = enableEntireFileCache;
	}

	public LocalRandomAccessLog(URI path) {
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
		lastRange = calculateRequestedRange(start, dir);
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
