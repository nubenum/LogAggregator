package de.nubenum.app.plugin.logaggregator.test.layers;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.RandomByteBuffer;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

public class TestHelper {
	public static abstract class AbstractEntryLog implements IEntryLog {

		@Override
		public void close() {
			// TODO Auto-generated method stub

		}

		@Override
		public void close(boolean keepInit) {
			// TODO Auto-generated method stub

		}
	}

	public static String str(RandomByteBuffer bytes) {
		return new String(bytes.getBytes(), StandardCharsets.UTF_8);
	}

	public static Object str(RandomByteBuffer bytes, Direction dir) {
		return new String(bytes.getOffsetBytes(), StandardCharsets.UTF_8);
	}

	public static IEntry getAt(IEntry reference, int offset, IEntry[] entries) {
		int index = IntStream.range(0, entries.length).filter(i -> entries[i] == reference).findFirst().orElse(-1);

		if (reference == Entry.FIRST)
			index = -1;
		if (reference == Entry.LAST)
			index = entries.length;
		index += offset;
		if (index < 0)
			return Entry.FIRST;
		if (index >= entries.length)
			return Entry.LAST;
		return entries[index];
	}
}
