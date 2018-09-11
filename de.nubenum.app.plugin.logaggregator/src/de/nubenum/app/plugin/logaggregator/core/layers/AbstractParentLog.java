package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import de.nubenum.app.plugin.logaggregator.core.AsyncEntryRetriever;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

public abstract class AbstractParentLog implements IEntryLog {
	protected List<? extends IChildLog> logs;
	private AsyncEntryRetriever retriever;

	public AbstractParentLog(List<? extends IChildLog> logs) {
		this.logs = logs;
		this.retriever = new AsyncEntryRetriever(logs.size());
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException {
		if (Math.abs(offset) > 1) {
			return getRandomAt(reference, offset);
		}
		IEntry entry = getNextAt(reference, offset);
		return entry;
	}

	protected IEntry getNextAt(IEntry reference, int direction) throws IOException {
		retriever.clear();
		for (IChildLog log : logs) {
			retriever.add(() -> log.getAt(reference, direction));
		}
		List<IEntry> stacked = retriever.get();

		Direction dir = Direction.get(direction);

		IEntry top = getTop(stacked, dir);
		return top;
	}

	protected IEntry getRandomAt(IEntry reference, int offset) throws IOException {
		IChildLog anchorLog = getAnchorLog(reference);
		IEntry entry = null;

		entry = anchorLog.getAt(reference, offset);
		if (Entry.isFirstOrLast(entry)) {
			for (IChildLog log : logs) {
				if (log != anchorLog) {
					entry = log.getAt(reference, offset);
					if (!Entry.isFirstOrLast(entry))
						break;
				}
			}
		}
		return entry;
	}

	private IEntry getTop(List<IEntry> entries, Direction dir) {
		Stream<IEntry> stream = entries.stream().filter(e -> !Entry.isFirstOrLast(e));
		if (dir == Direction.UP) {
			return stream.max((a, b) -> a.compareTo(b)).orElse(Entry.FIRST);
		}
		return stream.min((a, b) -> a.compareTo(b)).orElse(Entry.LAST);
	}

	private IChildLog getAnchorLog(IEntry reference) {
		for (IChildLog log : logs) {
			if (log.isOwnEntry(reference))
				return log;
		}
		return logs.get(0);
	}
}
