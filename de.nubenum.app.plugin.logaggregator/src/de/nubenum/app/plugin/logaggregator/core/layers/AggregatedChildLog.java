package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

public class AggregatedChildLog extends AbstractChildLog {

	public AggregatedChildLog(IEntryLog file) {
		super(file);
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException {
		return super.getAtEntry(reference, offset);
	}

	@Override
	public boolean isOwnEntry(IEntry entry) {
		return entry.getHost() != null;
	}
}
