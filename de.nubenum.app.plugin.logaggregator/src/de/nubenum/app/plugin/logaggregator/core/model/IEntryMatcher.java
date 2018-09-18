package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.regex.Pattern;

import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.StackedEntry;

/**
 * Containing the properties against which entries can be matched. Implementing
 * classes should override equals() to return true when two matchers contain the
 * same rules.
 *
 */
public interface IEntryMatcher {
	/**
	 * @return The minimal log level to match.
	 */
	public Level getMinLevel();

	/**
	 * @return The regex pattern to use to match message strings
	 */
	public Pattern getMessagePattern();

	/**
	 * @return The type of entries to match. Currently only used for
	 *         {@link StackedEntry}
	 */
	public Class<? extends IEntry> getType();

	/**
	 * Whether this matcher actually will not match all entries
	 *
	 * @return True if this matcher does not match all entries
	 */
	public boolean isRestrictive();
}
