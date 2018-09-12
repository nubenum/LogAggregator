package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;

public interface IEntry extends Comparable<IEntry> {
	/**
	 * Get the children of this entry.
	 *
	 * @return The List of children of this entry or an empty list if there are
	 *         none.
	 */
	public List<IEntry> getChildren();

	/**
	 * The timestamp of this entry.
	 *
	 * @return The timestamp
	 */
	public LogTime getLogTime();

	/**
	 * The original timestamp of this entry if the timestamp was spoofed because it
	 * was wrong. {@link #getLogTime()} returns the corrected timestamp in this
	 * case.
	 *
	 * @return The actual timestamp or in most cases null, if the timestamp was not
	 *         spoofed.
	 */
	public LogTime getActualLogTime();

	/**
	 * @return The log level of this entry.
	 */
	public Level getLevel();

	/**
	 * The complete path of this entry's host and source.
	 *
	 * @return The Path
	 */
	public String getPath();

	/**
	 * @return The first line of the message of this entry.
	 */
	public String getMessage();

	/**
	 * @return The entire available message concatenated from all children.
	 */
	public String getMessageComplete();

	/**
	 * Check whether this entry matches the matcher.
	 *
	 * @param matcher
	 *            The Matcher containing the match conditions.
	 * @return True if this entry matches.
	 */
	public boolean matches(IEntryMatcher matcher);

	/**
	 * Get the matches of the regex in the message of this entry.
	 *
	 * @param matcher
	 *            The Matcher containing the regex.
	 * @param complete
	 *            Whether to return matches for just the first line or the complete
	 *            message.
	 * @return The List of matches.
	 */
	public List<EntryMessageMatch> getMatches(IEntryMatcher matcher, boolean complete);

	/**
	 * Find links to classes and lines in stacktraces.
	 *
	 * @return The List of found links
	 */
	public List<EntryMessageLink> getLinks();

	/**
	 * The byte range in the source log file where this entry can be found. This
	 * will be used in lower levels to easily find adjacent entries.
	 *
	 * @return The file range
	 */
	public IFileRange getRange();

	/**
	 * @return The host that this entry originated from.
	 */
	public ILogHost getHost();

	/**
	 * @return The source log that this entry originated from.
	 */
	public ILogSource getSource();
}
