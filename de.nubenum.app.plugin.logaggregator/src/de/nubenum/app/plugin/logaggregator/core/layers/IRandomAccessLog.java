package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.EndOfLogReachedException;
import de.nubenum.app.plugin.logaggregator.core.IFilePosition;
import de.nubenum.app.plugin.logaggregator.core.RandomByteBuffer;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public interface IRandomAccessLog {
	void close() throws IOException;
	RandomByteBuffer getAt(IFilePosition start, Direction dir) throws EndOfLogReachedException, IOException;
	long getLength() throws IOException;
	long getLength(boolean forceRefresh) throws IOException;
}
