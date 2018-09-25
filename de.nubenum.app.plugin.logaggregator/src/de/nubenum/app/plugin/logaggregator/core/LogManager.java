package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.config.IConfig;
import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.layers.AggregatedChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.AggregatedGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.AggregatedParentLog;
import de.nubenum.app.plugin.logaggregator.core.layers.FilteredLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostParentLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostSourceChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.HostSourceGroupedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IChildLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IFilteredLog;
import de.nubenum.app.plugin.logaggregator.core.layers.IRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.LinedLog;
import de.nubenum.app.plugin.logaggregator.core.layers.LocalRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.layers.RotatedRandomAccessLog;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;

/**
 * Managing all log files, i.e. instantiating all file objects based on given
 * config and closing them again when necessary. This will notify listeners when
 * files or entries change (if the enableFileWatcher config option is not set to
 * false). This will also pass on UpdateEvents of the instantiated log files.
 *
 */
public class LogManager implements IUpdateInitiator, IUpdateListener {
	private IConfig config;
	private IFilteredLog log;
	private List<IUpdateListener> listeners = new ArrayList<>();
	private List<ILogDirectory> directories = new ArrayList<>();
	private PathWatcher watcher;

	private boolean enableMultithreading;
	private boolean enableEntireFileCache;
	private boolean enableFileWatcher;

	public LogManager() {
		this.log = new FilteredLog();
		this.watcher = new PathWatcher();
		this.watcher.addListener(e -> {
			if (e.getType() == Event.REFRESH) {
				try {
					SystemLog.log("Reloading due to file change on backend storage");
					setupLogs();
					listeners.forEach(l -> l.onUpdate(e));
				} catch (IOException exc) {
					listeners.forEach(l -> l.onUpdate(new UpdateEvent(exc)));
				}
			} else {
				SystemLog.log("Trying to update due to append on backend storage");
				try {
					close();
					listeners.forEach(l -> l.onUpdate(e));
				} catch (IOException exc) {
					listeners.forEach(l -> l.onUpdate(new UpdateEvent(exc)));
				}
			}
		});
	}

	/**
	 * Set a new config and load the indicated files. You can obtain a handle to the
	 * container log via {@link #getLog()}
	 *
	 * @param config
	 *            The configuration according to which to open the logs
	 * @throws IOException
	 *             When errors occur while opening the logs
	 */
	public synchronized void setConfig(IConfig config) throws IOException {
		this.config = config;
		setConfigOptions();
		SystemLog.log("Read config sources:"
				+ config.getSources().stream().map(l -> l.getName()).collect(Collectors.joining(",")));
		setupLogs();
	}

	private void setConfigOptions() {
		enableMultithreading = true;
		enableEntireFileCache = false;
		enableFileWatcher = true;
		if (config != null && config.getOptions() != null) {
			enableMultithreading = unboxBooleanDefault(config.getOptions().getEnableMultithreading(),
					enableMultithreading);
			enableEntireFileCache = unboxBooleanDefault(config.getOptions().getEnableEntireFileCache(),
					enableEntireFileCache);
			enableFileWatcher = unboxBooleanDefault(config.getOptions().getEnableFileWatcher(), enableFileWatcher);
			if (config.getOptions().getCustomLogTimeFormats() != null)
				LogTime.setCustomLogTimeFormats(config.getOptions().getCustomLogTimeFormats());
		}
	}

	private boolean unboxBooleanDefault(Boolean option, boolean defaultValue) {
		return option != null ? option : defaultValue;
	}

	private List<IRandomAccessLog> getSourceFiles(ILogHost host, ILogSource source) throws IOException {
		Path path = Paths.get(config.getLocation());

		ILogDirectory dir = new LocalLogDirectory(path, host, source);
		addDirectory(dir);

		List<IRandomAccessLog> list = new ArrayList<>();
		try {
			List<File> files = dir.getSourceFiles(source);
			Iterator<File> it = files.iterator();
			while (it.hasNext()) {
				File file = it.next();
				LocalRandomAccessLog log = new LocalRandomAccessLog(file.toPath(), enableEntireFileCache);
				log.addListener(this);
				list.add(log);
			}
		} catch (IOException e) {
			// TODO lazy loading not found ignored?
			if (!source.getIgnoreNotFound())
				throw new IOException(Paths.get(host.getName(), source.getName())
						+ " was not found. You can add the tag ignoreNotFound=\"true\" to the respective source in the config file to ignore this error.",
						e);
		}
		SystemLog.log(host.getName() + source.getName() + ": "
				+ list.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));
		return list;
	}

	private void addDirectory(ILogDirectory dir) {
		if (!directories.contains(dir)) {
			directories.add(dir);
			if (enableFileWatcher)
				watcher.addPath(dir.getPath());
		}
	}

	private List<IChildLog> getHostSourceLogs(ILogHost host) throws IOException {
		List<? extends ILogSource> sources = config.getSources();
		List<IChildLog> logs = new ArrayList<>();

		for (ILogSource source : sources) {
			List<IRandomAccessLog> files = getSourceFiles(host, source);
			if (!files.isEmpty()) {
				IRandomAccessLog sourceFile = new RotatedRandomAccessLog(files);
				LinedLog lined = new LinedLog(sourceFile, host, source);
				lined.addListener(this);
				IChildLog log = new HostSourceChildLog(new HostSourceGroupedLog(lined), source);
				logs.add(log);
			}
		}
		return logs;
	}

	private List<IChildLog> getHostLogs() throws IOException {
		List<? extends ILogHost> hosts = config.getHosts();
		List<IChildLog> logs = new ArrayList<>();

		for (ILogHost host : hosts) {
			List<IChildLog> files = getHostSourceLogs(host);
			if (!files.isEmpty()) {
				IChildLog log = new HostChildLog(new HostGroupedLog(new HostParentLog(files, enableMultithreading)),
						host);
				logs.add(log);
			}
		}
		return logs;
	}

	private void setupLogs() throws IOException {
		close();
		List<IChildLog> files = getHostLogs();
		if (files.isEmpty())
			throw new IOException("No logs were found at all. Please check whether the log location is available.");
		IEntryLog agg = new AggregatedChildLog(
				new AggregatedGroupedLog(new AggregatedParentLog(files, enableMultithreading)));
		log.setLog(agg);
	}

	/**
	 * Close all open files, stop watching them and cleanup.
	 * @throws IOException
	 */
	public void close() throws IOException {
		watcher.stop();
		log.close();
		directories.clear();
	}

	/**
	 * Get the log that acts as a container for the set up logs
	 *
	 * @return The log
	 */
	public IFilteredLog getLog() {
		return log;
	}

	@Override
	public void onUpdate(UpdateEvent event) {
		if (event.getType() == Event.START) {
			try {
				setupLogs();
			} catch (IOException e) {
				SystemLog.log(e);
			}
		}
		listeners.forEach(l -> l.onUpdate(event));
	}

	@Override
	public void addListener(IUpdateListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IUpdateListener listener) {
		listeners.remove(listener);
	}

}
