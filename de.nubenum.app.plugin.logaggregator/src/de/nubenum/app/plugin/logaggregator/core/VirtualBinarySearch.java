package de.nubenum.app.plugin.logaggregator.core;

import java.util.function.BiFunction;

import de.nubenum.app.plugin.logaggregator.core.model.Direction;

public class VirtualBinarySearch<Element> {
	private static final Long MAX_STEPS = 1000L;
	private Long jump = 1000L;
	private Direction searchDir = Direction.DOWN;

	private BiFunction<Element, Long, Element> retriever;
	private BiFunction<Element, Element, Direction> comparator;

	public void setElementRetriever(BiFunction<Element, Long, Element> retriever) {
		this.retriever = retriever;
	}

	public void setComparator(BiFunction<Element, Element, Direction> comparator) {
		this.comparator = comparator;
	}

	public void setSearchDirection(Long jump) {
		this.jump = jump;
		this.searchDir = Direction.get(jump);
	}

	public Element search(Element target, Element pivot) {
		Long offset = jump;
		Direction oldDir = searchDir;
		Direction newDir;
		Element newPivot;
		boolean binaryMode = false;

		for(int i=0; i<MAX_STEPS; i++) {
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
			if (i % 100 == 0 && Thread.interrupted())
				return null;
		}

		return null;
	}
}
