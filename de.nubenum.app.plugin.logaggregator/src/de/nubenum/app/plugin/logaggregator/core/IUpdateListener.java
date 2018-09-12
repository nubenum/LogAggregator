package de.nubenum.app.plugin.logaggregator.core;

/**
 * Classes that listen to updates must implement this interface.
 *
 */
public interface IUpdateListener {
	/**
	 * Called when an update is available.
	 * @param event The UpdateEvent containing information about the update occurring
	 */
	void onUpdate(UpdateEvent event);
}
