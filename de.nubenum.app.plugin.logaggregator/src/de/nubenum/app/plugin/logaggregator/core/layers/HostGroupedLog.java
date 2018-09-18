package de.nubenum.app.plugin.logaggregator.core.layers;

import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.entry.DeduplicatedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * A GroupedLog that groups duplicate entries occurring in different sources of one host.
 *
 */
public class HostGroupedLog extends AbstractGroupedLog {

	public HostGroupedLog(IEntryLog file) {
		super(file);
	}

	@Override
	protected IEntry makeGroup(List<IEntry> entries) {
		return new DeduplicatedEntry(entries);
	}

	@Override
	protected boolean isGroupable(List<IEntry> group, IEntry next, Direction dir) {
		IEntry first = group.get(0);
		if (first.getLogTime().equals(next.getLogTime()) && first.getMessage().equals(next.getMessage()))
			return true;
		return false;
	}

	@Override
	protected int getAvgGroupSize() {
		return 2;
	}

	@Override
	protected IEntry degroupedReference(IEntry reference, Direction dir) {
		if (reference instanceof DeduplicatedEntry)
			return ((DeduplicatedEntry) reference).getBoundChild(dir);
		return reference;
	}

}
