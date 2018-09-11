package de.nubenum.app.plugin.logaggregator.core.model;

import java.nio.file.Paths;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;

public class DeduplicatedEntry extends GroupedEntry {

	public DeduplicatedEntry(List<IEntry> children) {
		super(children);
	}

	@Override
	public ILogHost getHost() {
		return getChildren().get(0).getHost();
	}

	@Override
	public ILogSource getSource() {
		return null;
	}

	@Override
	public IFileRange getRange() {
		return null;
	}

	@Override
	public String getPath() {
		return Paths.get(getHost().getName(),
				bracedChildrenPath(c -> c.getSource() == null ? null : c.getSource().getName())).toString();
	}

	@Override
	public String getMessageComplete() {
		return getChildren().get(0).getMessageComplete();
	}
}
