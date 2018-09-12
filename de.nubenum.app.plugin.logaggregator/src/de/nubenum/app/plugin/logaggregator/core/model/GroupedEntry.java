package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.List;

/**
 * An entry that has multiple children that were grouped together.
 *
 */
public abstract class GroupedEntry extends Entry {
	private List<IEntry> children;

	public GroupedEntry(List<IEntry> children) {
		this.children = children;
	}

	@Override
	public String getMessage() {
		return getChildren().get(0).getMessage();
	}

	@Override
	public LogTime getLogTime() {
		return getChildren().get(0).getLogTime();
	}

	@Override
	public LogTime getActualLogTime() {
		return getChildren().get(0).getActualLogTime();
	}

	@Override
	public List<IEntry> getChildren() {
		return children;
	}

	@Override
	public Level getLevel() {
		return getChildren().get(0).getLevel();
	}

	protected boolean matchesChildren(IEntryMatcher filter) {
		for (IEntry child : children) {
			if (child.matches(filter))
				return true;
		}
		return false;
	}

	@Override
	public boolean matches(IEntryMatcher filter) {
		//TODO 3x nested types
		boolean typeMatch = (filter.getType().isInstance(getChildren().get(0)) || filter.getType().isInstance(this));
		if (typeMatch && matchesProperties(filter))
			return true;
		return false;
	}

	/**
	 * Get the first or last child
	 * @param dir The direction in which to get the child
	 * @return The first child for UP, the last child for DOWN
	 */
	public IEntry getBoundChild(Direction dir) {
		if (dir == Direction.DOWN)
			return getChildren().get(getChildren().size()-1);
		return getChildren().get(0);
	}
}
