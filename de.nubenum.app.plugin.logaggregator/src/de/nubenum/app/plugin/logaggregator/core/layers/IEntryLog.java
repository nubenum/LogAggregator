package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.InitializedCloseable;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * Representing an entry-based log with relative access to entries.
 *
 */
public interface IEntryLog extends InitializedCloseable {
	/**
	 * The distance between relative entries up to which all intermediate entries
	 * should be calculated. For greater distances, a heuristic approach should be
	 * used to avoid the calculation of too many entries at once.
	 */
	public static int LOOKAROUND_BOUNDS = 100;
	/**
	 * The maximum amount of children an entry should have. Excess children will be
	 * truncated to save memory. Items should be truncated in the middle and not at
	 * the end.
	 */
	public static int TRUNCATE_GROUP_SIZE = 10000;

	/**
	 * Get a new IEntry by the given reference
	 *
	 * @param reference
	 *            The reference IEntry
	 * @param offset
	 *            The offset from the reference at which to get the new IEntry
	 * @return The new IEntry
	 * @throws IOException
	 *             If the backend storage is unavailable
	 * @throws InterruptedException
	 *             If the thread was interrupted
	 */
	IEntry getAt(IEntry reference, int offset) throws IOException, InterruptedException;
}
