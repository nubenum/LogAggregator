package de.nubenum.app.plugin.logaggregator.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.core.IUpdateInitiator;
import de.nubenum.app.plugin.logaggregator.core.IUpdateListener;
import de.nubenum.app.plugin.logaggregator.core.InitializedCloseable;
import de.nubenum.app.plugin.logaggregator.core.LogManager;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.config.IConfigFile;
import de.nubenum.app.plugin.logaggregator.core.config.XmlConfigFile;
import de.nubenum.app.plugin.logaggregator.core.layers.entries.IFilteredLog;

public class LogController implements IUpdateInitiator, InitializedCloseable {
	IConfigFile configFile;
	LogManager manager;
	List<IUpdateListener> listeners = new ArrayList<>();

	LogController() {
		try {
			configFile = new XmlConfigFile();
		} catch (IOException e) {
			SystemLog.log(e);
		}
		manager = new LogManager();
		manager.addListener(event -> {
			listeners.forEach(l -> l.onUpdate(event));
		});
	}

	public IFilteredLog getLog() {
		return manager.getLog();
	}

	public void setConfigFile(File file) {
		new Thread(() -> {
			if (file != null)
				configFile.setFile(file);
			try {
				configFile.read();
				manager.setConfig(configFile.getConfig());
				listeners.forEach(l -> l.onUpdate(new UpdateEvent(Event.STOP)));
			} catch (IOException e) {
				listeners.forEach(l -> l.onUpdate(new UpdateEvent(e)));
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

	@Override
	public void close(boolean keepInit) {
		manager.close(keepInit);
	}
}
