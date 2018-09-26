package de.nubenum.app.plugin.logaggregator.core;

public interface InitializedCloseable {

	void close();

	void close(boolean keepInit);

}