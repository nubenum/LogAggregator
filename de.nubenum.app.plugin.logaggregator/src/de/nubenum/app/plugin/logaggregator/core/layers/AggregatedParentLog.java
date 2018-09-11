package de.nubenum.app.plugin.logaggregator.core.layers;

import java.util.List;

public class AggregatedParentLog extends AbstractParentLog {

	public AggregatedParentLog(List<IChildLog> logs) {
		super(logs);
	}
}
