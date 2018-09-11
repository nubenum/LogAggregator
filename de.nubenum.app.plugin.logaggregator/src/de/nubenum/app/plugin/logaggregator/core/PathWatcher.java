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

public class PathWatcher implements IUpdateInitiator {
	private List<IUpdateListener> listeners = new ArrayList<>();
	private WatchService watcher;

	public PathWatcher() {
	}

	public void addPath(Path path) {
		if (watcher == null)
			newWatcher();
		try {
			path.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
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
				WatchEvent.Kind<?> priorityKind = null;
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.ENTRY_CREATE
							|| priorityKind == null && kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						priorityKind = kind;
					}
				}
				if (priorityKind == StandardWatchEventKinds.ENTRY_CREATE)
					listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.REFRESH)));
				else if (priorityKind == StandardWatchEventKinds.ENTRY_MODIFY)
					listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.APPEND)));

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
