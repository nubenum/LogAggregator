package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public class FilePosition implements IFilePosition {
	public static final IFilePosition FIRST = new FilePosition();
	public static final IFilePosition LAST = new FilePosition();

	private int partOffset = 0;
	private long byteOffset = 0;

	public FilePosition() {
		return;
	}

	public FilePosition(int partOffset, long byteOffset) {
		this.partOffset = partOffset;
		this.byteOffset = byteOffset;
	}

	@Override
	public int getPartOffset() {
		return partOffset;
	}

	@Override
	public long getByteOffset() {
		return byteOffset;
	}

	@Override
	public int compareTo(IFilePosition other) {
		if (this == other)
			return 0;
		if (this == FilePosition.FIRST || other == FilePosition.LAST)
			return -1;
		if (this == FilePosition.LAST || other == FilePosition.FIRST)
			return 1;
		int part = this.getPartOffset()-other.getPartOffset();
		if (part != 0)
			return part;
		long bytes = this.getByteOffset()-other.getByteOffset();
		if (bytes != 0)
			return Direction.get(bytes).getValue();
		return 0;
	}

	@Override
	public IFilePosition offset(int offset, Direction dir) {
		return offset(offset*dir.getValue());
	}

	@Override
	public IFilePosition offset(int offset) {
		return new FilePosition(partOffset, byteOffset+offset);
	}

	@Override
	public boolean isTopmost() {
		if (partOffset == 0 && byteOffset == 0)
			return true;
		return false;
	}

	@Override
	public int distance(IFilePosition other) {
		if (other.getPartOffset() != this.getPartOffset())
			throw new IllegalArgumentException("No known distance");
		return (int) (this.getByteOffset()-other.getByteOffset());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IFilePosition) {
			return compareTo((IFilePosition) other) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) (partOffset+byteOffset%1e6);
	}

	@Override
	public String toString() {
		return partOffset+":"+byteOffset;
	}

}
