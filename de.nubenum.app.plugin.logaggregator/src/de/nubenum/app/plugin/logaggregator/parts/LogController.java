package de.nubenum.app.plugin.logaggregator.parts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.nubenum.app.plugin.logaggregator.config.IConfigFile;
import de.nubenum.app.plugin.logaggregator.config.XmlConfigFile;
import de.nubenum.app.plugin.logaggregator.core.IUpdateInitiator;
import de.nubenum.app.plugin.logaggregator.core.IUpdateListener;
import de.nubenum.app.plugin.logaggregator.core.LogManager;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.layers.IFilteredLog;

public class LogController implements IUpdateInitiator {
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

	public void close() {
		manager.close();
	}
}
