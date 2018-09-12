package de.nubenum.app.plugin.logaggregator.core;

/**
 * Classes that allow listeners to be attached to them because they publish
 * updates should implement this interface.
 *
 */
public interface IUpdateInitiator {
	/**
	 * Add a listener to this object.
	 * @param listener The listener
	 */
	public void addListener(IUpdateListener listener);

	/**
	 * Remove a listener from this object.
	 * @param listener The listener to be removed
	 */
	public void removeListener(IUpdateListener listener);
}
