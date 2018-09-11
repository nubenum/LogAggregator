package de.nubenum.app.plugin.logaggregator.core.layers;

import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

public interface IChildLog extends IEntryLog {
	public boolean isOwnEntry(IEntry reference);
}
