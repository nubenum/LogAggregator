package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.VirtualBinarySearch;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

/**
 * A log that will cooperate with a ParentLog in order to aggregate multiple
 * children to one parent. Particularly, it provides functionality to search the
 * nearest entry to a foreign entry, i.e. an entry that did not originate from
 * this child log and can thus not directly be used as a reference.
 *
 */
public abstract class AbstractChildLog implements IChildLog {
	private VirtualBinarySearch<IEntry> search;
	private IEntryLog file;
	private IEntry lastShown = Entry.LAST;

	public AbstractChildLog(IEntryLog file) {
		this.file = file;
		this.search = new VirtualBinarySearch<>();
		this.search.setElementRetriever((pivot, offset) -> {
			try {
				IEntry entry = getAtEntry(pivot, Math.toIntExact(offset));
				if (Entry.isFirstOrLast(entry))
					return null;
				return entry;
			} catch (IOException e) {
				SystemLog.log(e);
				return null;
			}
		});
		this.search.setComparator((a, b) -> {
			return Direction.get(a.compareTo(b));
		});
	}

	/**
	 * This will try to find the nearest entry based on the reference, either
	 * directly if it is a proper entry, or by delegating a search if it is a
	 * foreign entry.
	 *
	 * @param reference The reference IEntry
	 * @param offset The offset
	 * @return The found entry
	 * @throws IOException If the backing storage is unavailable
	 */
	protected IEntry getAtBest(IEntry reference, int offset) throws IOException {
		IEntry entry = null;
		if (reference == Entry.FIRST || reference == Entry.LAST) {
			entry = getAtEntry(reference, offset);
		} else if (isOwnEntry(reference)) {
			entry = getAtEntry(reference, offset);
		} else {
			entry = getAtForeignEntry(reference, offset);
			// TODO
			if (Math.abs(offset) > 1) {
				entry = file.getAt(entry, offset);
			}
		}
		lastShown = entry;
		return entry;
	}

	/**
	 * Get an entry by a proper reference and offset taking into account the {@link IEntryLog#LOOKAROUND_BOUNDS}.
	 * @param reference The IEntry reference
	 * @param offset The offset
	 * @return The obtained IEntry
	 * @throws IOException If the backing storage is unavailable
	 */
	protected IEntry getAtEntry(IEntry reference, int offset) throws IOException {

		if (Math.abs(offset) > LOOKAROUND_BOUNDS) {
			return file.getAt(reference, offset);
		}
		int direction = Direction.get(offset).getValue();

		for (int i = 0; i < Math.abs(offset); i++) {
			reference = file.getAt(reference, direction);
			if (Entry.isFirstOrLast(reference))
				break;
		}
		return reference;
	}

	/**
	 * Get an entry by a foreign reference by employing a binary search.
	 * @param reference The IEntry reference
	 * @param offset The offset
	 * @return The obtained IEntry
	 * @throws IOException If the backing storage is unavailable
	 */
	protected IEntry getAtForeignEntry(IEntry reference, int offset) throws IOException {
		Direction dir = Direction.get(offset);
		Direction dirFromLastShown = Direction.get(reference.compareTo(lastShown));

		search.setSearchDirection((long) Math.abs(offset) * dirFromLastShown.getValue());
		IEntry entry = search.search(reference, lastShown);

		if (entry == null)
			return Entry.getFirstOrLast(dir);
		if (Entry.isFirstOrLast(entry))
			return entry;

		if (Direction.get(entry.compareTo(reference)) != dir)
			entry = file.getAt(entry, dir.getValue());
		return entry;
	}
}
