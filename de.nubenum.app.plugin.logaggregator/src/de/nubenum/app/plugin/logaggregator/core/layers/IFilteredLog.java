package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * An entry-based log that can be filtered and whose source log can be set
 * dynamically.
 *
 */
public interface IFilteredLog extends IEntryLog {
	/**
	 * Get the next matching IEntry and its distance to the reference.
	 *
	 * @param reference
	 *            The reference IEntry
	 * @param offset
	 *            The direction in which to search
	 * @return The next matching entry and its distance
	 * @throws IOException
	 *             When the backend storage is unavailable
	 * @throws InterruptedException
	 */
	public ReferenceOffset getMatchingAt(IEntry reference, int offset) throws IOException, InterruptedException;

	/**
	 * Set the matcher on which to base the filtering
	 *
	 * @param matcher
	 *            The matcher
	 */
	public void setMatcher(IEntryMatcher matcher);

	/**
	 * @return The matcher currently set
	 */
	public IEntryMatcher getMatcher();

	/**
	 * Toggle whether only matching entries should be returned by
	 * {@link #getAt(IEntry, int)}
	 *
	 * @param enable
	 */
	public void toggleFilter(boolean enable);

	/**
	 * Set the backing log
	 * @param log The backing log
	 */
	public void setLog(IEntryLog log);
}
