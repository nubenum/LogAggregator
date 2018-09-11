package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;

public interface IEntry extends Comparable<IEntry> {

	public List<IEntry> getChildren();

	public LogTime getLogTime();
	public LogTime getActualLogTime();
	public Level getLevel();
	public String getPath();

	public String getMessage();
	public String getMessageComplete();
	public boolean matches(IEntryMatcher matcher);
	public List<EntryMessageMatch> getMatches(IEntryMatcher matcher, boolean complete);
	public List<EntryMessageLink> getLinks();

	public IFileRange getRange();

	public ILogHost getHost();
	public ILogSource getSource();
}
