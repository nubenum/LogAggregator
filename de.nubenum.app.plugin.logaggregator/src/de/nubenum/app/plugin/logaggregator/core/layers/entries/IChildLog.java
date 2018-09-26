package de.nubenum.app.plugin.logaggregator.core.layers.entries;

import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
/**
 * An entry-based log that acts as the child of a parent log, i.e. multiple child logs will be aggregated to one parent log.
 *
 */
public interface IChildLog extends IEntryLog {
	/**
	 * Check whether an entry belongs to this log
	 * @param reference An arbitrary IEntry.
	 * @return Whether the given IEntry originated from this child log.
	 */
	public boolean isOwnEntry(IEntry reference);
}
