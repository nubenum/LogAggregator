package de.nubenum.app.plugin.logaggregator.core.layers.entries;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * A ChildLog that represents the aggregated logs of one host.
 *
 */
public class HostChildLog extends AbstractChildLog {
	private ILogHost host;

	public HostChildLog(IEntryLog file, ILogHost host) {
		super(file);
		this.host = host;
	}

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException, InterruptedException {
		return super.getAtBest(reference, offset);
	}

	@Override
	public boolean isOwnEntry(IEntry reference) {
		if (reference.getHost() == host)
			return true;
		return false;
	}

}
