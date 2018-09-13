package de.nubenum.app.plugin.logaggregator.core;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;

/**
 * Watches a number of paths a notifies listeners to updates to these files with
 * an UpdateEvent of type Event.REFRESH (new file created) or Event.APPEND (file
 * modified)
 *
 */
public class PathWatcher implements IUpdateInitiator {
	private List<IUpdateListener> listeners = new ArrayList<>();
	private WatchService watcher;
	private long lastUpdateTs = 0;
	private static final int MIN_UPDATE_INTERVAL = 5000;

	public PathWatcher() {
	}

	/**
	 * Add a new path to be watched. Watching will be started immediately.
	 *
	 * @param path
	 *            The path.
	 */
	public void addPath(Path path) {
		if (watcher == null)
			newWatcher();
		try {
			path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException x) {
			return;
		}
	}

	public void reset() {
		try {
			if (watcher != null) {
				watcher.close();
				watcher = null;
			}
		} catch (IOException e) {
			return;
		}
	}

	private void newWatcher() {
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			watch();
		} catch (IOException e) {
			return;
		}
	}

	private void watch() {
		new Thread(() -> {
			while (true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException | ClosedWatchServiceException x) {
					return;
				}
				if (System.currentTimeMillis() - lastUpdateTs > MIN_UPDATE_INTERVAL) {
					lastUpdateTs = System.currentTimeMillis();
					WatchEvent.Kind<?> priorityKind = null;
					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						SystemLog.log(event.context().toString());
						if (kind == StandardWatchEventKinds.ENTRY_CREATE
								|| priorityKind == null && kind == StandardWatchEventKinds.ENTRY_MODIFY) {
							priorityKind = kind;
						}
					}
					if (priorityKind == StandardWatchEventKinds.ENTRY_CREATE)
						listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.REFRESH)));
					else if (priorityKind == StandardWatchEventKinds.ENTRY_MODIFY)
						listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.APPEND)));
				}
				boolean valid = key.reset();
				if (!valid)
					return;
			}
		}).start();
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
