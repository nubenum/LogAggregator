package de.nubenum.app.plugin.logaggregator.core.model.entry;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.model.FileRange;
import de.nubenum.app.plugin.logaggregator.core.model.IFileRange;

/**
 * A GroupedEntry created in the HostSourceGroupLog layer.
 *
 */
public class StackedEntry extends GroupedEntry {
	private String messageComplete = null;

	public StackedEntry(List<IEntry> children) {
		super(children);
	}

	@Override
	public IFileRange getRange() {
		IFileRange first = getChildren().get(0).getRange();
		IFileRange last = getChildren().get(getChildren().size()-1).getRange();
		if (first != null && last != null) {
			return new FileRange(first.getTop(), last.getBottom());
		}
		return null;
	}

	@Override
	public ILogHost getHost() {
		return getChildren().get(0).getHost();
	}

	@Override
	public ILogSource getSource() {
		return getChildren().get(0).getSource();
	}

	@Override
	public String getPath() {
		return Paths.get(getHost().getName(), getSource().getName()).toString();
	}

	@Override
	public String getMessageComplete() {
		if (messageComplete == null) {
			String lines = getChildren().stream()
					.map(c -> c.getMessageComplete())
					.collect(Collectors.joining("\n"));
			messageComplete = lines;
		}
		return messageComplete;
	}

	@Override
	public boolean contains(IEntry other) {
		if (getHost() != null && getHost().equals(other.getHost()) && 
				getSource() != null && getSource().equals(other.getSource())) {
			IFileRange thisRange = getRange();
			IFileRange otherRange = other.getRange();
			return thisRange != null && otherRange != null && thisRange.inRange(otherRange.getTop());
		}
		return false;
	}
}
