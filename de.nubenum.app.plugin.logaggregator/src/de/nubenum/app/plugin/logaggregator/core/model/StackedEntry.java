package de.nubenum.app.plugin.logaggregator.core.model;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;

public class StackedEntry extends GroupedEntry {
	private String messageComplete = null;

	public StackedEntry(List<IEntry> children) {
		super(children);
	}

	@Override
	public IFileRange getRange() {
		//TODO range?
		return getChildren().get(0).getRange();
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
}
