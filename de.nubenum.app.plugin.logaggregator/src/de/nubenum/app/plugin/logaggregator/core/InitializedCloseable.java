package de.nubenum.app.plugin.logaggregator.core;

/**
 * Represents a resource that can be closed in two different modes: Retaining
 * the initialization and closing completely.
 *
 */
public interface InitializedCloseable {

	/**
	 * This is equivalent to calling <code>close(false)</code>
	 */
	default void close() {
		close(false);
	}

	/**
	 * Close the resource
	 * @param keepInit Whether to keep the initialization in order to be able to continue working with the resource
	 */
	void close(boolean keepInit);

}