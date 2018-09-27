package de.nubenum.app.plugin.logaggregator.core;

import java.util.function.BiFunction;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

/**
 * A generic implementation of an exponential search algorithm that works with
 * relative and absolute references to the elements if they have a definitive
 * order. I.e. while searching, the pivot will be compared to the target element
 * and will approach it as near as possible, finding exactly the sought element
 * or the nearest one contained in the collection.
 *
 * @param <Element>
 *            The type representing one element in the collection to be
 *            searched.
 */
public class VirtualBinarySearch<Element> {
	private static final Long MAX_STEPS = 1000L;
	private Long jump = 1000L;
	private Direction searchDir = Direction.DOWN;

	private BiFunction<Element, Long, Element> retriever;
	private BiFunction<Element, Element, Direction> comparator;

	/**
	 * Set the Function that will be used to obtain a new element from the
	 * collection.
	 *
	 * @param retriever
	 *            This is a Function that will take an element and an offset to
	 *            retrieve and return the new element.
	 */
	public void setElementRetriever(BiFunction<Element, Long, Element> retriever) {
		this.retriever = retriever;
	}

	/**
	 * Set the Function that will be used to compare to elements.
	 *
	 * @param comparator
	 *            This is a Function that takes to elements and should return -1 if
	 *            the first element appears before the second element in the
	 *            collection, 1 if it appears afterwards, and 0 if they are equal.
	 */
	public void setComparator(BiFunction<Element, Element, Direction> comparator) {
		this.comparator = comparator;
	}

	/**
	 * Optionally set the search Direction to speed up the search.
	 *
	 * @param jump
	 *            The offset with which to jump to get the next pivot on the first
	 *            iteration.
	 */
	public void setSearchDirection(Long jump) {
		this.jump = jump;
		this.searchDir = Direction.get(jump);
	}

	/**
	 * Execute the search. This might take some time.
	 *
	 * @param target
	 *            The element to be found in the collection (usually one that is not
	 *            actually contained in the collection, but is still comparable,
	 *            e.g. an element that only contains a key and no value or an
	 *            element that originates from another collection of elements)
	 * @param pivot
	 *            The element at which to start the search. This can always be the
	 *            first or last element of the collection or an element that is
	 *            conjectured to be near the target.
	 * @return The element that is nearest to the target
	 * @throws InterruptedException
	 *             If the thread was interrupted with ThreadInterruptor while
	 *             searching
	 */
	public Element search(Element target, Element pivot) throws InterruptedException {
		Long offset = jump;
		Direction oldDir = searchDir;
		Direction newDir;
		Element newPivot;
		boolean binaryMode = false;

		for (int i = 0; i < MAX_STEPS; i++) {
			newPivot = retriever.apply(pivot, offset);
			if (newPivot == null) {
				offset /= 2;
			} else {
				newDir = comparator.apply(target, newPivot);
				if (newDir == Direction.NONE) {
					return newPivot;
				} else if (oldDir == newDir) {
					if (!binaryMode)
						offset *= 2;
				} else {
					binaryMode = true;
					offset /= -2;
				}
				if (offset == 0) {
					return newPivot;
				}

				pivot = newPivot;
				oldDir = newDir;
			}
			if (ThreadInterruptor.isInterrupted()) {
				throw new InterruptedException();
			}
		}

		return null;
	}
}
