package de.nubenum.app.plugin.logaggregator.core.layers.entries;

import java.util.List;

/**
 * A ParentLog that aggregates all host logs.
 *
 */
public class AggregatedParentLog extends AbstractParentLog {

	public AggregatedParentLog(List<? extends IChildLog> logs, boolean enableMultithreading) {
		super(logs, enableMultithreading);
	}

	public AggregatedParentLog(List<? extends IChildLog> logs) {
		super(logs);
	}

}
