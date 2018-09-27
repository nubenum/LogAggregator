package de.nubenum.app.plugin.logaggregator.core.config;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Options to alter the behavior of LogAggregator.
 *
 */
public interface IOptions {
	/**
	 * Whether the log files should be read using multiple threads. This will
	 * substantially speed up search times, particularly when accessing log files
	 * via network. The default is true, i.e. implementing classes should return
	 * true if nothing was explicitly set.
	 *
	 * @return True if multithreading should be enabled.
	 */
	Boolean getEnableMultithreading();

	/**
	 * Whether files should be copied to RAM entirely when opened instead of only
	 * randomly accessing small byte ranges. This is strongly discouraged, as it
	 * will quickly lead to heap overflows. However, it might help when log files
	 * are accessed through an unstable network connection. The default is false,
	 * i.e. implementing classes should return false if nothing was explicitly set.
	 *
	 * @return True if files should be loaded entirely.
	 */
	@Deprecated
	Boolean getEnableEntireFileCache();

	/**
	 * Whether the log files should be watched for changes and new entries (after a
	 * delay) should be loaded into the view. This might not always work with
	 * network stored files. This is not implemented for the smb:// file handler.
	 * The default is true, i.e. implementing classes should return true if nothing
	 * was explicitly set.
	 *
	 * @return True if files should be watched.
	 */
	Boolean getEnableFileWatcher();

	/**
	 * Whether all log files should be closed immediately upon finished processing
	 * (e.g. completed search, scrolling finished...) instead of being kept open
	 * while LogAggregator is running. This might slow down access times but can
	 * help with file systems exercising read locks (cf. oplocks for SMB: with
	 * autoclosing disabled, other users won't be able to edit the log files that
	 * you have opened from the network drive). The default is true, i.e.
	 * implementing classes should return true if nothing was explicitly set.
	 *
	 * @return True if files should be closed as soon as possible.
	 */
	Boolean getEnableAutoClose();

	/**
	 * Additionally defined log timestamp formats, in case the predefined ones do
	 * not cover all formats found in the log files. Use patterns as in
	 * {@link DateTimeFormatter}
	 *
	 * @return A List of pattern strings
	 */
	List<String> getCustomLogTimeFormats();

	/**
	 *
	 * @param enableMultithreading
	 *            Whether multithreading should be enabled.
	 */
	void setEnableMultithreading(Boolean enableMultithreading);

	/**
	 *
	 * @param enableEntireFileCache
	 *            Whether files should be loaded entirely to RAM.
	 */

	@Deprecated
	void setEnableEntireFileCache(Boolean enableEntireFileCache);

	/**
	 *
	 * @param enableFileWatcher
	 *            Whether files should be watched for updates.
	 */
	void setEnableFileWatcher(Boolean enableFileWatcher);

	/**
	 *
	 * @param enableAutoClose
	 *            Whether immediate closing of files should be enabled.
	 */
	void setEnableAutoClose(Boolean enableAutoClose);

	/**
	 *
	 * @param customLogTimeFormats
	 *            Set the List of pattern strings.
	 */
	void setCustomLogTimeFormats(List<String> customLogTimeFormats);

}
