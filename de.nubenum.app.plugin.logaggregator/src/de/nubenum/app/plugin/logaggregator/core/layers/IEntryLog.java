package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

public interface IEntryLog {
	public static int LOOKAROUND_BOUNDS = 100;
	public static int TRUNCATE_GROUP_SIZE = 10000;
	IEntry getAt(IEntry reference, int offset) throws IOException;
}
