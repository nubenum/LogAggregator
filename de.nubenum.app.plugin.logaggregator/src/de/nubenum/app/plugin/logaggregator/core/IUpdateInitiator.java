package de.nubenum.app.plugin.logaggregator.core;

public interface IUpdateInitiator {
	public void addListener(IUpdateListener listener);
	public void removeListener(IUpdateListener listener);
}
