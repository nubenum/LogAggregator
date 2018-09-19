package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.CacheProvider;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;

/**
 * An entry-based log that groups multiple consecutive entries together, based
 * on rules to be defined in concrete implementations of this abstract class.
 * Too large groups will be truncated in the middle to avoid high memory
 * consumption. This class employs multiple caches to avoid rerequesting and
 * discarding entries multiple times. It uses three cache access methods:
 * <ul>
 * <li>Search the grouped cache by ReferenceOffset before obtaining anything
 * else</li>
 * <li>Search the adjacent cache for the first entry of the new group that was
 * discarded the last time as a non-matching entry</li>
 * <li>Search the grouped cache by child after obtaining the first child entry
 * of the to-be-created group</li>
 * </ul>
 *
 */
public abstract class AbstractGroupedLog implements IEntryLog {

	protected static final int MAX_GROUP_SIZE = 10000000;

	protected IEntryLog file;
	private CacheProvider groupedCache;
	private CacheProvider adjacentCache;

	protected AbstractGroupedLog(IEntryLog file) {
		this.file = file;
		this.groupedCache = new CacheProvider();
		this.adjacentCache = new CacheProvider();
	}

	@Override
	public IEntry getAt(IEntry groupedReference, int groupedOffset) throws IOException, InterruptedException {

		int degroupedOffset = groupedOffset;
		int direction = groupedOffset;
		if (Math.abs(groupedOffset) > 1) {
			direction = 0;
			degroupedOffset *= getAvgGroupSize();
		}

		IEntry reference = degroupedReference(groupedReference, Direction.get(groupedOffset));

		IEntry cached = groupedCache.getAt(reference, degroupedOffset);
		if (cached != null) {
			return cached;
		}

		IEntry entry = adjacentCache.getAt(reference, degroupedOffset);
		if (entry == null) {
			entry = file.getAt(reference, degroupedOffset);
			if (Entry.isFirstOrLast(entry)) {
				return entry;
			}
			cached = groupedCache.getByChildAt(entry);
			if (cached != null)
				return cached;
		}

		entry = getGroupedAt(entry, direction);

		if (!Entry.isFirstOrLast(reference)) {
			groupedCache.put(reference, degroupedOffset, entry);
			if (reference.getSource() != null)
				groupedCache.put(degroupedReference(entry, Direction.get(-degroupedOffset)), -degroupedOffset, groupedReference);
		}

		return entry;
	}

	private IEntry getGroupedAt(IEntry reference, int direction) throws IOException, InterruptedException {
		List<IEntry> newGroup;
		if (direction == 0) {
			newGroup = getGroupableEntriesAt(reference, 1);
			newGroup.remove(0);
			newGroup.addAll(0, getGroupableEntriesAt(reference, -1));
		} else {
			newGroup = getGroupableEntriesAt(reference, direction);
		}
		if (newGroup.size() > 1)
			return makeGroup(newGroup);
		return newGroup.get(0);
	}

	private List<IEntry> getGroupableEntriesAt(IEntry reference, int direction) throws IOException, InterruptedException {
		LinkedList<IEntry> newGroup = new LinkedList<>();
		newGroup.add(reference);

		IEntry entry;
		IEntry overflowEntry = null;
		Direction dir = Direction.get(direction);

		int i;
		for (i = 1; i < MAX_GROUP_SIZE; i++) {

			entry = file.getAt(reference, direction);
			if (Entry.isFirstOrLast(entry))
				break;

			if (!isGroupable(newGroup, entry, dir)) {
				adjacentCache.put(reference, direction, entry);
				break;
			}
			reference = entry;
			overflowEntry = addEntryToLimitedListInDirection(newGroup, reference, dir, overflowEntry);
		}
		addOverflowEntry(newGroup, overflowEntry, i);
		return newGroup;
	}

	private IEntry addEntryToLimitedListInDirection(LinkedList<IEntry> list, IEntry entry, Direction dir,
			IEntry overflowEntry) {
		if (dir == Direction.DOWN) {
			if (list.size() >= TRUNCATE_GROUP_SIZE)
				overflowEntry = entry;
			else
				list.add(entry);
		} else {
			if (list.size() >= TRUNCATE_GROUP_SIZE) {
				IEntry last = list.removeLast();
				if (overflowEntry == null)
					overflowEntry = last;
			}
			list.addFirst(entry);
		}
		return overflowEntry;
	}

	private void addOverflowEntry(LinkedList<IEntry> newGroup, IEntry overflowEntry, int entryCount) {
		if (overflowEntry != null) {
			newGroup.add(new LinedEntry("[TRUNCATED ~" + (entryCount - TRUNCATE_GROUP_SIZE) + " LINES]"));
			newGroup.add(overflowEntry);
		}
	}

	/**
	 * Used for heuristics for offsets > 1
	 * @return The estimated average group size
	 */
	protected abstract int getAvgGroupSize();

	/**
	 * From a List of IEntries, make a grouped entry, i.e. instantiate a child of GroupedEntry
	 * @param entries The List of IEntries that the new group is to be made up of
	 * @return An IEntry containing the given entries as children
	 */
	protected abstract IEntry makeGroup(List<IEntry> entries);

	/**
	 * Check whether a given IEntry is groupable with the given existing members of the group
	 * @param entries The List of existing groupable entries with at least one entry
	 * @param next The entry to be checked
	 * @param dir The direction in which to append the entry
	 * @return True if the given entry should be added to this group, false otherwise
	 */
	protected abstract boolean isGroupable(List<IEntry> entries, IEntry next, Direction dir);

	/**
	 * From a grouped entry that was created in this class, extract the first or last child to hand over to lower levels
	 * @param reference An IEntry that may possibly be a grouped entry
	 * @param dir The direction in which to get the bound entry, Direction.UP for the first child, Direction.DOWN for the last child
	 * @return The first or last child depending on the Direction if reference is a grouped entry that was created in this class, the unaltered reference otherwise
	 */
	protected abstract IEntry degroupedReference(IEntry reference, Direction dir);
}
