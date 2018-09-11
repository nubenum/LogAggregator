package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.CacheProvider;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;

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
	public IEntry getAt(IEntry groupedReference, int groupedOffset) throws IOException {

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

		groupedCache.put(reference, degroupedOffset, entry);
		if (reference.getSource() != null)
			groupedCache.put(degroupedReference(entry, Direction.get(-degroupedOffset)), -degroupedOffset, groupedReference);

		return entry;
	}

	private IEntry getGroupedAt(IEntry reference, int direction) throws IOException {
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

	private List<IEntry> getGroupableEntriesAt(IEntry reference, int direction) throws IOException {
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

	private IEntry addEntryToLimitedListInDirection(LinkedList<IEntry> list, IEntry entry, Direction dir, IEntry overflowEntry) {
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
			newGroup.add(new LinedEntry("[TRUNCATED ~"+(entryCount-TRUNCATE_GROUP_SIZE)+" LINES]"));
			newGroup.add(overflowEntry);
		}
	}

	protected abstract int getAvgGroupSize();

	protected abstract IEntry makeGroup(List<IEntry> entries);

	protected abstract boolean isGroupable(List<IEntry> entries, IEntry next, Direction dir);

	protected abstract IEntry degroupedReference(IEntry reference, Direction dir);
}
