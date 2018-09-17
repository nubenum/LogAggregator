package de.nubenum.app.plugin.logaggregator.config;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
		makeLogSources(sources, logSources);
		//setIgnoreNotFounds(files, hosts, sources);
	}

	private void makeLogHosts(Set<File> hosts, List<ILogHost> logHosts) {
		for (File host : hosts) {
			ILogHost logHost = new XmlLogHost();
			logHost.setName(location.relativize(host.toPath().getParent()).toString());
		}
	}

	private void makeLogSources(HashMap<File, Integer> sources, List<ILogSource> logSources) {
		// TODO Auto-generated method stub

	}

	private void extractRelativeSources(Set<File> hosts, List<File> absoluteSources, HashMap<File, Integer> relativeSources) {
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

	private void extractHosts(List<File> files, final Set<File> hosts) {
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
				System.out.println(host.getParent()+"|"+parentHost.toString());
				return host.getParent().startsWith(parentHost.toString());
			};
			if (hosts.stream().anyMatch(isChildOf))
				i.remove();
		}
	}

	private void setIgnoreNotFounds(List<File> files, final Set<ILogHost> hosts, final Set<ILogSource> sources) {
		return;
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
