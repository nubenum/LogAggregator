package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The default implementation of the IEntryMatcher.
 *
 */
public class EntryMatcher implements IEntryMatcher {
	private Level minLevel = Level.ALL;
	private Pattern pattern;
	private Class<? extends IEntry> type;

	public EntryMatcher(Level minLevel, String pattern, Class<? extends IEntry> type) {
		this.minLevel = minLevel;
		this.pattern = (pattern.length() > 0) ? Pattern.compile(pattern, Pattern.MULTILINE) : null;
		this.type = type;
	}

	@Override
	public Level getMinLevel() {
		return minLevel;
	}

	@Override
	public Pattern getMessagePattern() {
		return pattern;
	}

	@Override
	public Class<? extends IEntry> getType() {
		return type;
	}

	@Override
	public boolean isRestrictive() {
		if (getMinLevel() == Level.ALL
				&& getMessagePattern() == null && getType() == IEntry.class)
			return false;
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof EntryMatcher) {
			EntryMatcher matcher = (EntryMatcher) other;
			if (minLevel == matcher.getMinLevel()
					&& (pattern == matcher.getMessagePattern() ||
					pattern != null && matcher.getMessagePattern() != null && pattern.toString().equals(matcher.getMessagePattern().toString()))
					&& type == matcher.getType())
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(minLevel, pattern, type);
	}
}
