package de.nubenum.app.plugin.logaggregator.core.layers;

import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.entry.CondensedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 *  GroupedLog that could in the future group similar entries across hosts. This currently does nothing (but caching).
 *
 */
public class AggregatedGroupedLog extends AbstractGroupedLog {

	public AggregatedGroupedLog(IEntryLog file) {
		super(file);
	}

	@Override
	protected int getAvgGroupSize() {
		return 1;
	}

	@Override
	protected IEntry makeGroup(List<IEntry> entries) {
		return new CondensedEntry(entries);
	}

	@Override
	protected boolean isGroupable(List<IEntry> entries, IEntry next, Direction dir) {
		//TODO condensed?
		return false;
	}

	@Override
	protected IEntry degroupedReference(IEntry reference, Direction dir) {
		if (reference instanceof CondensedEntry)
			return ((CondensedEntry) reference).getBoundChild(dir);
		return reference;
	}


}
