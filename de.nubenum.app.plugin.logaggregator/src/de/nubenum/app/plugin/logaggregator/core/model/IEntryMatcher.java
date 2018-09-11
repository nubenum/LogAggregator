package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.regex.Pattern;


public interface IEntryMatcher {
	public Level getMinLevel();
	public Pattern getMessagePattern();
	public Class<? extends IEntry> getType();
	public boolean isRestrictive();
}
