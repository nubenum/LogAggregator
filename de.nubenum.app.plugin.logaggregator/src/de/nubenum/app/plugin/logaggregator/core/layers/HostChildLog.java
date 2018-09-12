package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

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
	public IEntry getAt(IEntry reference, int offset) throws IOException {
		return super.getAtBest(reference, offset);
	}

	@Override
	public boolean isOwnEntry(IEntry reference) {
		if (reference.getHost() == host)
			return true;
		return false;
	}

}
