package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import de.nubenum.app.plugin.logaggregator.core.AsyncEntryRetriever;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * An entry-based log that will aggregate multiple ChildLogs, i.e. pull together
 * the entries of all child logs like a zip. For sequential entries, it has to
 * be made sure that no entries from any child are omitted, for offsets > 1, a
 * random roughly matching entry might be chosen.
 *
 */
public abstract class AbstractParentLog implements IEntryLog {
	protected List<? extends IChildLog> logs;
	private AsyncEntryRetriever retriever;

	public AbstractParentLog(List<? extends IChildLog> logs, boolean enableMultithreading) {
		this.logs = logs;
		this.retriever = new AsyncEntryRetriever(enableMultithreading ? logs.size() : 0);
	}

	public AbstractParentLog(List<? extends IChildLog> logs) {
		this(logs, true);
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException, InterruptedException {
		if (Math.abs(offset) > 1) {
			return getRandomAt(reference, offset);
		}
		IEntry entry = getNextAt(reference, offset);
		return entry;
	}

	/**
	 * Get the nearest entry in the given direction of all children
	 *
	 * @param reference
	 *            The IEntry reference
	 * @param direction
	 *            The direction in which to search
	 * @return The next entry
	 * @throws IOException
	 *             If the backing storage is unavailable
	 * @throws InterruptedException
	 */
	protected IEntry getNextAt(IEntry reference, int direction) throws IOException, InterruptedException {
		retriever.clear();
		for (IChildLog log : logs) {
			retriever.add(() -> log.getAt(reference, direction));
		}
		List<IEntry> stacked = retriever.get();

		Direction dir = Direction.get(direction);

		IEntry top = getTop(stacked, dir);
		return top;
	}

	/**
	 * Get an entry at a certain offset using heuristics, taking preferably an entry
	 * from the child log that the reference entry originated from (to avoid binary
	 * search)
	 *
	 * @param reference
	 *            The IEntry reference
	 * @param offset
	 *            The offset, ideally larger than 1
	 * @return The found entry that has approximately the given offset from the
	 *         given reference
	 * @throws IOException
	 *             If the backing storage is unavailable
	 * @throws InterruptedException
	 */
	protected IEntry getRandomAt(IEntry reference, int offset) throws IOException, InterruptedException {
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
