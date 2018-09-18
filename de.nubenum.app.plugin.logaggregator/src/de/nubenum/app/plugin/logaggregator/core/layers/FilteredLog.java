package de.nubenum.app.plugin.logaggregator.core.layers;

import java.io.IOException;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;
import de.nubenum.app.plugin.logaggregator.core.model.entry.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;

/**
 * The default implementation of IFilteredLog.
 *
 */
public class FilteredLog implements IFilteredLog {
	public static int LOOKAROUND_BOUNDS = 10000;
	private IEntryLog file = null;
	private IEntryMatcher matcher;
	private boolean filtered = false;

	@Override
	public IEntry getAt(IEntry reference, int offset) throws IOException {
		if (file == null)
			return null;

		if (Math.abs(offset) > LOOKAROUND_BOUNDS) {
			reference = getMatchingAt(reference, offset, filtered).getEntry();
		}
		int direction = Direction.get(offset).getValue();

		for(int i=0;i<Math.abs(offset);i++) {
			reference = getMatchingAt(reference, direction, filtered).getEntry();
		}
		return reference;
	}

	private ReferenceOffset getMatchingAt(IEntry reference, int offset, boolean filtered) throws IOException {
		int i = 0;
		do {
			reference = file.getAt(reference, offset);
			offset = Direction.get(offset).getValue();
			i += offset;
			if (Entry.isFirstOrLast(reference))
				break;
			if (i % 100 == 0 && Thread.interrupted())
				return null;
		} while (matcher != null && filtered && !reference.matches(matcher));
		return new ReferenceOffset(reference, i);
	}

	@Override
	public ReferenceOffset getMatchingAt(IEntry reference, int offset) throws IOException {
		return getMatchingAt(reference, offset, true);
	}

	@Override
	public void setMatcher(IEntryMatcher filter) {
		this.matcher = filter;
		if (!this.matcher.isRestrictive())
			this.matcher = null;
	}

	@Override
	public void setLog(IEntryLog file) {
		this.file = file;
	}

	@Override
	public void toggleFilter(boolean filtered) {
		this.filtered = filtered;
	}

	@Override
	public IEntryMatcher getMatcher() {
		return matcher;
	}

}
