package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * An immutable pair representing a reference with an offset.
 *
 */
public class ReferenceOffset {
	private SimpleImmutableEntry<IEntry, Integer> data;

	public ReferenceOffset(IEntry reference, int offset) {
		this.data = new SimpleImmutableEntry<IEntry, Integer>(reference, offset);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ReferenceOffset)
			return data.equals(((ReferenceOffset) other).data);
		return false;
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	public IEntry getEntry() {
		return data.getKey();
	}

	public int getOffset() {
		return data.getValue();
	}
}
