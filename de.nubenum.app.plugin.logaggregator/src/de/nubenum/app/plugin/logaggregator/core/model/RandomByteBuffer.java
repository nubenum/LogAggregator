package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.Arrays;

/**
 * A wrapper around a byte array that stores the position from which this array
 * was extracted and a pointer to a position within this array to avoid repeated
 * copying of byte arrays
 *
 */
public class RandomByteBuffer {

	private byte[] bytes = null;
	private int offset = 0;
	private Direction dir = Direction.DOWN;
	private IFileRange range = null;

	public RandomByteBuffer() {
		return;
	}

	/**
	 * Instantiate with a raw byte buffer
	 *
	 * @param buffer
	 *            The byte buffer
	 */
	public RandomByteBuffer(byte[] buffer) {
		this.bytes = buffer;
	}

	/**
	 * Make a clone of the given RandomByteBuffer. The contained byte array will not
	 * be copied, but referenced.
	 *
	 * @param buffer
	 *            The existing buffer to clone
	 */
	public RandomByteBuffer(RandomByteBuffer buffer) {
		deepCopyOf(buffer);
	}

	/**
	 *
	 * @param buffer
	 * @param range
	 * @param offset
	 * @param dir
	 */
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

	/**
	 * The offset of the pointer within the wrapped byte array
	 *
	 * @return The offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * The range where this byte array was extracted from the backing storage
	 *
	 * @return The range
	 */
	public IFileRange getRange() {
		return range;
	}

	/**
	 * @return The length of wrapped byte array
	 */
	public int getLength() {
		return bytes.length;
	}

	/**
	 * @return The remaining length from the offset of the pointer to the top or
	 *         bottom of the byte array, depending on the set Direction of this
	 *         buffer
	 */
	public int getOffsetLength() {
		if (dir == Direction.DOWN)
			return bytes.length - offset;
		return offset + 1;
	}

	/**
	 * @return The raw byte array wrapped by this buffer
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * The use of this method is inefficient and discouraged since a new copy of the
	 * byte array is created.
	 *
	 * @return A new byte array representing the remaining bytes from the offset to
	 *         the top or bottom (depending on set Direction of this buffer).
	 */
	public byte[] getOffsetBytes() {
		if (offset == 0 && dir == Direction.DOWN)
			return bytes;
		int top = 0;
		int bottom = bytes.length;
		if (dir == Direction.UP)
			bottom = offset + 1;
		else if (dir == Direction.DOWN)
			top = offset;

		return Arrays.copyOfRange(bytes, top, bottom);
	}

	/**
	 * Concat another buffer with this one in the set Direction of this buffer. The
	 * buffers should be adjacent and have the appropriate range and dir information
	 * set, other cases will lead to undefined behavior.
	 *
	 * @param other
	 *            The other buffer to be appended
	 */
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
		byte[] result = Arrays.copyOf(a, a.length + b.length);
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
