package de.nubenum.app.plugin.logaggregator.core;

import java.util.Arrays;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public class RandomByteBuffer {

	private byte[] bytes = null;
	private int offset = 0;
	private Direction dir = Direction.DOWN;
	private IFileRange range = null;


	public RandomByteBuffer() {
		return;
	}

	public RandomByteBuffer(byte[] buffer) {
		this.bytes = buffer;
	}

	public RandomByteBuffer(RandomByteBuffer buffer) {
		deepCopyOf(buffer);
	}

	public RandomByteBuffer(RandomByteBuffer buffer, IFileRange range, int offset, Direction dir) {
		this.bytes = buffer.bytes;
		this.range = range;
		this.offset = offset;
		this.dir = dir;
	}

	public RandomByteBuffer(RandomByteBuffer buffer, int offset, Direction dir) {
		deepCopyOf(buffer);
		this.offset = offset;
		this.dir = dir;
	}

	public RandomByteBuffer(RandomByteBuffer buffer, IFileRange range) {
		deepCopyOf(buffer);
		this.range = range;
	}

	public int getOffset() {
		return offset;
	}

	public IFileRange getRange() {
		return range;
	}

	public int getLength() {
		return bytes.length;
	}

	public int getOffsetLength() {
		if (dir == Direction.DOWN)
			return bytes.length - offset;
		return offset+1;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public byte[] getOffsetBytes() {
		if (offset == 0 && dir == Direction.DOWN)
			return bytes;
		int top = 0;
		int bottom = bytes.length;
		if (dir == Direction.UP)
			bottom = offset+1;
		else if (dir == Direction.DOWN)
			top = offset;

		return Arrays.copyOfRange(bytes, top, bottom);
	}

	public void concatOrdered(RandomByteBuffer other) {
		if (this.bytes == null) {
			deepCopyOf(other);
		} else if (other.bytes != null) {
			if (this.range == null || other.range == null || dir == null)
				throw new IllegalArgumentException("buffer has no range information and can not be concatenated");
			if (dir == Direction.UP) {
				this.bytes = concatBytes(other.bytes, this.bytes);
				this.range = new FileRange(other.range.getTop(), bytes.length);
				this.offset += other.bytes.length;
			} else {
				this.bytes = concatBytes(this.bytes, other.bytes);
				this.range = new FileRange(this.range.getTop(), bytes.length);
			}
		}
	}

	private byte[] concatBytes(byte[] a, byte[] b) {
		byte[] result = Arrays.copyOf(a, a.length+b.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	private void deepCopyOf(RandomByteBuffer b) {
		this.bytes = b.bytes;
		this.range = b.range;
		this.offset = b.offset;
		this.dir = b.dir;
	}
}
