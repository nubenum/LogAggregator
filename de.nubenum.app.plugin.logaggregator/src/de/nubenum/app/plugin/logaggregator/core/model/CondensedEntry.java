package de.nubenum.app.plugin.logaggregator.core.model;

import java.nio.file.Paths;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;

public class CondensedEntry extends GroupedEntry {

	public CondensedEntry(List<IEntry> children) {
		super(children);
	}

	@Override
	public ILogHost getHost() {
		return null;
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
		return Paths.get(bracedChildrenPath(c -> c.getSource() == null ? null : c.getSource().getName()),
				bracedChildrenPath(c -> c.getHost() == null ? null : c.getHost().getName())).toString();
	}

	@Override
	public String getMessageComplete() {
		return getChildren().get(0).getMessageComplete();
	}

}
