package de.nubenum.app.plugin.logaggregator.core.layers;

import java.util.List;

/**
 * A ParentLog that aggregates all sources of a single host.
 *
 */
public class HostParentLog extends AbstractParentLog {

	public HostParentLog(List<IChildLog> logs) {
		super(logs);
	}
}
