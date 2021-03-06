package de.nubenum.app.plugin.logaggregator.core.layers.entries;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * A ChildLog representing a single log source on a single host.
 *
 */
public class HostSourceChildLog extends AbstractChildLog {

	private ILogSource source;

	public HostSourceChildLog(IEntryLog file, ILogSource source) {
		super(file);
		this.source = source;
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException, InterruptedException {
		return super.getAtBest(reference, offset);
	}

	@Override
	public boolean isOwnEntry(IEntry reference) {
		if (reference.getSource() == source)
			return true;
		return false;
	}

}
