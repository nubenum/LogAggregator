package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.VirtualBinarySearch;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;

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
				return null;
			}
		});
		this.search.setComparator((a, b) -> {
			return Direction.get(a.compareTo(b));
		});
	}

	protected IEntry getAtBest(IEntry reference, int offset) throws IOException {
		IEntry entry = null;
		if (reference == Entry.FIRST || reference == Entry.LAST) {
			entry = getAtEntry(reference, offset);
		} else if (isOwnEntry(reference)) {
			entry = getAtEntry(reference, offset);
		} else {
			entry = getAtForeignEntry(reference, offset);
			//TODO
			if (Math.abs(offset) > 1) {
				entry = file.getAt(entry, offset);
			}
		}
		lastShown = entry;
		return entry;
	}

	protected IEntry getAtEntry(IEntry reference, int offset) throws IOException {

		if (Math.abs(offset) > LOOKAROUND_BOUNDS) {
			return file.getAt(reference, offset);
		}
		int direction = Direction.get(offset).getValue();

		for(int i=0;i<Math.abs(offset);i++) {
			reference = file.getAt(reference, direction);
			if (Entry.isFirstOrLast(reference))
				break;
		}
		return reference;
	}

	protected IEntry getAtForeignEntry(IEntry reference, int offset) throws IOException {
		Direction dir = Direction.get(offset);
		Direction dirFromLastShown = Direction.get(reference.compareTo(lastShown));

		search.setSearchDirection((long) Math.abs(offset)*dirFromLastShown.getValue());
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
