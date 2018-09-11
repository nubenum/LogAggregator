package de.nubenum.app.plugin.logaggregator.core;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public class FileRange implements IFileRange {

	private int length;
	private IFilePosition top;
	private IFilePosition bottom;

	public FileRange(IFilePosition start, int length, Direction dir) {
		this.length = length;
		if (dir == Direction.UP) {
			this.top = start.offset(-length+1);
			this.bottom = start.offset(1);
		} else {
			this.top = start;
			this.bottom = start.offset(length);
		}
	}

	public FileRange(IFilePosition start, int length) {
		this(start, Math.abs(length), Direction.get(length));
	}

	public FileRange(long start, int length) {
		this(new FilePosition(0, start), Math.abs(length), Direction.get(length));
	}

	public FileRange(IFilePosition top, IFilePosition bottom) {
		if (top.getPartOffset() != bottom.getPartOffset())
			throw new IllegalArgumentException("impossible to define ranges over multiple parts");
		this.length = (int) (bottom.getByteOffset()-top.getByteOffset());
		this.top = top;
		this.bottom = bottom;
	}

	@Override
	public int compareTo(IFileRange other) {
		if (this == other)
			return 0;
		int top = this.getTop().compareTo(other.getTop());
		if (top != 0)
			return top;
		int bottom = this.getBottom().compareTo(other.getBottom());
		if (bottom != 0)
			return bottom;
		return 0;
	}

	@Override
	public IFilePosition getTop() {
		return top;
	}

	@Override
	public IFilePosition getBottom() {
		return bottom;
	}

	@Override
	public IFilePosition getStart(Direction dir) {
		if (dir == Direction.UP) {
			return top.offset(-1);
		}
		return bottom;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public boolean inRange(IFilePosition pos) {
		if (getTop().compareTo(pos) <= 0 && getBottom().compareTo(pos) > 0)
			return true;
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IFileRange) {
			return compareTo((IFileRange) other) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return top.hashCode()+bottom.hashCode();
	}

	@Override
	public String toString() {
		return top.toString() + "-" + bottom.toString();
	}

	@Override
	public IFileRange clip(long length) {
		IFilePosition newTop = top;
		IFilePosition newBottom = bottom;
		if (top.getByteOffset() < 0)
			newTop = new FilePosition(top.getPartOffset(), 0);
		if (bottom.getByteOffset() >= length)
			newBottom = new FilePosition(bottom.getPartOffset(), length);
		return new FileRange(newTop, newBottom);
	}

}
