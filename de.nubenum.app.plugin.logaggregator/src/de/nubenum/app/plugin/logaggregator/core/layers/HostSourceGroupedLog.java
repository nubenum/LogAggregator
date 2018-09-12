package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.StackedEntry;

/**
 * Implementation of GroupedLog that groups stack traces, i.e. multiple log
 * lines without timestamp. This will also try to fix wrong timestamps, i.e.
 * timestamps that are in wrong order within this single log or grouped entries
 * without any timestamp.
 *
 */
public class HostSourceGroupedLog extends AbstractGroupedLog {

	public HostSourceGroupedLog(IEntryLog file) {
		super(file);
	}

	@Override
	public IEntry getAt(IEntry reference, int stackedOffset) throws IOException {
		IEntry entry = super.getAt(reference, stackedOffset);
		if (Entry.isFirstOrLast(entry))
			return entry;
		entry = fixMessedUpTimestamps(reference, stackedOffset, entry);
		assert (entry.getLogTime() != null) : "StackEntry without LogTime: " + entry.getRange().getTop().getByteOffset()
		+ " | " + entry.toString();
		return entry;
	}

	private IEntry fixMessedUpTimestamps(IEntry reference, int offset, IEntry entry) {
		if (entry.getLogTime() != null) {
			if (reference.getLogTime() != null) {
				Direction actual = Direction.get(entry.getLogTime().compareTo(reference.getLogTime()));
				if (actual != Direction.NONE && actual != Direction.get(offset)) {
					LinedEntry first = (LinedEntry) degroupedReference(entry, Direction.UP);
					first.setLogTime(reference.getLogTime());
					System.out.println("Timestamp spoofed: " + entry);
				}
			}
		}
		if (entry.getLogTime() == null && reference.getLogTime() != null) {
			LinedEntry first = (LinedEntry) degroupedReference(entry, Direction.UP);
			first.setLogTime(LogTime.NONE);
			first.setLogTime(reference.getLogTime());
			System.out.println("Grouped timestamp spoofed: " + entry);
		}
		return entry;
	}

	@Override
	protected IEntry makeGroup(List<IEntry> entries) {
		return new StackedEntry(entries);
	}

	@Override
	protected boolean isGroupable(List<IEntry> group, IEntry next, Direction dir) {
		// TODO stack traces with timestamps
		if (dir == Direction.DOWN && next.getLogTime() == null)
			return true;
		if (dir == Direction.UP && group.get(0).getLogTime() == null)
			return true;
		return false;
	}

	@Override
	protected int getAvgGroupSize() {
		return 5;
	}

	@Override
	protected IEntry degroupedReference(IEntry reference, Direction dir) {
		if (reference instanceof StackedEntry)
			return ((StackedEntry) reference).getBoundChild(dir);
		return reference;
	}

}
