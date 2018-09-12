package de.nubenum.app.plugin.logaggregator.core.layers;

import java.util.List;

/**
 * A ParentLog that aggregates all host logs.
 *
 */
public class AggregatedParentLog extends AbstractParentLog {

	public AggregatedParentLog(List<IChildLog> logs) {
		super(logs);
	}
}
