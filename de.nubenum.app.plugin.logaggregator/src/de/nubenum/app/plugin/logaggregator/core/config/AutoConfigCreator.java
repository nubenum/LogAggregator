package de.nubenum.app.plugin.logaggregator.core.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class AutoConfigCreator {
	private Path location;

	public AutoConfigCreator(Path location) {
		this.location = location;
	}

	public IConfig create() {
		IConfig config = new XmlConfig();
		config.setLocation(location.toString());
		List<ILogHost> hosts = new ArrayList<>();
		List<ILogSource> sources = new ArrayList<>();
		analyzeHostsAndSources(hosts, sources);
		config.setHosts(new ArrayList<>(hosts));
		config.setSources(new ArrayList<>(sources));
		return config;
	}

	private void analyzeHostsAndSources(final List<ILogHost> logHosts, final List<ILogSource> logSources) {
		List<File> files = traverse(location.toFile());
		Set<File> hosts = new HashSet<>();
		HashMap<File, Integer> sources = new HashMap<>();
		extractHosts(files, hosts);
		extractRelativeSources(hosts, files, sources);
		makeLogHosts(hosts, logHosts);
		makeLogSources(sources, logSources, logHosts);
	}

	private void makeLogHosts(final Set<File> hosts, final List<ILogHost> logHosts) {
		for (File host : hosts) {
			ILogHost logHost = new XmlLogHost();
			logHost.setName(location.relativize(host.toPath()).toString());
			logHosts.add(logHost);
		}
		logHosts.sort((a,b) -> a.getName().compareTo(a.getName()));
	}

	private void makeLogSources(final HashMap<File, Integer> sources, final List<ILogSource> logSources, final List<ILogHost> logHosts) {
		int hostCount = logHosts.size();
		for (Map.Entry<File, Integer> entry : sources.entrySet()) {
			ILogSource logSource = new XmlLogSource();
			if (entry.getValue() != hostCount)
				logSource.setIgnoreNotFound(true);

			String nameWoExtension = getNameWithoutExtension(entry.getKey());
			String parent = entry.getKey().getParent();
			String path = parent == null ? nameWoExtension : Paths.get(parent, nameWoExtension).toString();
			/*
			int nameLen = nameWoExtension.length();
			int i = 0;
			String extensionFragment;
			do {
				extensionFragment = entry.getKey().getName().substring(nameLen, nameLen+i);
				i++;
			} while (anyOtherLogSourceStartsWith(path+extensionFragment, logSources) && nameLen+i <= entry.getKey().getName().length());*/
			logSource.setName(path);
			logSources.add(logSource);
		}
		logSources.sort((a,b) -> a.getName().compareTo(a.getName()));
	}

	private boolean anyOtherLogSourceStartsWith(String path, final List<ILogSource> logSources) {
		return logSources.stream().anyMatch(s -> s.getName().startsWith(path));
	}

	private void extractRelativeSources(final Set<File> hosts, final List<File> absoluteSources, final HashMap<File, Integer> relativeSources) {
		for (File source : absoluteSources) {
			File parent = findParent(source, hosts);
			if (parent == null)
				continue;
			File relative = parent.toPath().relativize(source.toPath()).toFile();
			relativeSources.putIfAbsent(relative, 0);
			relativeSources.put(relative, relativeSources.get(relative)+1);
		}
	}

	private File findParent(File source, Set<File> hosts) {
		return hosts.stream().filter(host -> source.toString().startsWith(host.toString())).findFirst().orElse(null);
	}

	private List<File> traverse(File dir) {
		if (!dir.isDirectory())
			return Arrays.asList();

		File[] in = dir.listFiles(f -> {
			return !f.getName().startsWith(".");
		});
		List<File> files = new ArrayList<>();
		List<File> out = new ArrayList<>();
		for (File file : in) {
			if (file.isDirectory()) {
				out.addAll(traverse(file));
			} else {
				files.add(file);
			}
		}
		out.addAll(filterRotated(files));
		return out;
	}

	private List<File> filterRotated(List<File> files) {
		List<File> filtered = new ArrayList<>();

		sortByName(files);
		String firstOfType = null;

		for (File file : files) {
			if (firstOfType == null || !file.getName().startsWith(firstOfType)) {
				filtered.add(file);
				firstOfType = getNameWithoutExtension(file);
			}
		}
		return filtered;
	}

	private void extractHosts(final List<File> files, final Set<File> hosts) {
		for (File file : files) {
			hosts.add(file.getParentFile());
		}
		filterNestedHosts(hosts);
	}

	private void filterNestedHosts(final Set<File> hosts) {
		Iterator<File> i = hosts.iterator();
		while (i.hasNext()) {
			File host = i.next();
			Predicate<File> isChildOf = parentHost -> {
				return host.getParent().startsWith(parentHost.toString());
			};
			if (hosts.stream().anyMatch(isChildOf))
				i.remove();
		}
	}

	private void sortByName(final List<File> files) {
		files.sort((a, b) -> a.getName().compareTo(b.getName()));
	}

	private String getNameWithoutExtension(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf(".");
		if (pos > 0)
			return name.substring(0, pos);
		return name;
	}


}
