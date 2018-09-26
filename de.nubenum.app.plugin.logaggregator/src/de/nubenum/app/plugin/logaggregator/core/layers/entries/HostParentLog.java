package de.nubenum.app.plugin.logaggregator.core.layers.entries;

import java.util.List;

/**
 * A ParentLog that aggregates all sources of a single host.
 *
 */
public class HostParentLog extends AbstractParentLog {

	public HostParentLog(List<? extends IChildLog> logs, boolean enableMultithreading) {
		super(logs, enableMultithreading);
	}

	public HostParentLog(List<? extends IChildLog> logs) {
		super(logs);
	}

}
