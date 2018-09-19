package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * This represents the aggregated log of all hosts to layers above.
 * Particularly, the logic of the ChildLog is reused to enable binary search by
 * timestamp for jumping to a date.
 *
 */
public class AggregatedChildLog extends AbstractChildLog {

	public AggregatedChildLog(IEntryLog file) {
		super(file);
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException, InterruptedException {
		return super.getAtEntry(reference, offset);
	}

	@Override
	public boolean isOwnEntry(IEntry entry) {
		return entry.getHost() != null;
	}
}
