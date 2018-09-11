package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;

public interface IFilteredLog extends IEntryLog {
	public ReferenceOffset getMatchingAt(IEntry reference, int offset) throws IOException;
	public void setMatcher(IEntryMatcher matcher);
	public IEntryMatcher getMatcher();
	public void toggleFilter(boolean enable);
	public void setLog(IEntryLog log);
}
