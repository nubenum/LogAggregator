package de.nubenum.app.plugin.logaggregator.core.layers.bytes;

import java.io.IOException;
import java.net.URI;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.Utils;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

/**
 * An implementation for a single file that can be accessed randomly by byte.
 *
 */
public class SmbRandomAccessLog extends AbstractSingleRandomAccessLog {
	private URI path;
	private SmbRandomAccessFile file;
	private long length = -1;

	private RandomByteBuffer entireFileCache = null;
	public SmbRandomAccessLog(URI path, boolean enableEntireFileCache) {
		this.path = path;
		this.enableEntireFileCache = enableEntireFileCache;
	}

	public SmbRandomAccessLog(URI path) {
		this(path, false);
	}

	private void openFile() throws IOException {
		if (file == null) {
			int shareAccess = SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE;
			this.file = new SmbRandomAccessFile(path.toString(), "r", shareAccess);
			if (length == -1) {
				int newLength = (int) getLength(true);
				listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.SIZE, newLength)));
				//SystemLog.log("Opening " + path.getFileName());
				if (enableEntireFileCache) {
					//TODO
					//entireFileCache = new RandomByteBuffer(Files.readAllBytes(path));
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
			//return new RandomByteBuffer(Arrays.copyOfRange(entireFileCache.getBytes(), (int) top, (int)top+len));
		}

		openFile();
		byte[] buf = new byte[len];
		file.seek(top);
		file.readFully(buf);
		return new RandomByteBuffer(buf);
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
			length = file.length();
		}
		return length;
	}

	@Override
	public String toString() {
		return Utils.getFileName(path);
	}
}
