package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;

public abstract class AbstractLogDirectory implements IUpdateInitiator, ILogDirectory {
	private static final Pattern naturalSortEligible = Pattern.compile("(.*?)(\\d+)");
	protected Path path;
	protected ILogHost host;
	private List<IUpdateListener> listeners;

	public AbstractLogDirectory(Path location, ILogHost host, ILogSource source) {
		this.host = host;
		this.path = location.resolve(host.getName());
		Path sourceParent = Paths.get(source.getName()).getParent();
		if (sourceParent != null)
			this.path = this.path.resolve(sourceParent);
	}

	protected abstract List<File> getUnfilteredSourceFiles(ILogSource source) throws IOException;

	@Override
	public List<File> getSourceFiles(ILogSource source) throws IOException {
		List<File> files = getUnfilteredSourceFiles(source);

		String match = Paths.get(source.getName()).getFileName().toString();
		files = files.stream().filter(f -> f.getName().startsWith(match)).collect(Collectors.toList());

		files.sort((a, b) -> {
			if (isCurrentFile(a, source)) return 1;
			if (isCurrentFile(b, source)) return -1;

			int natural = naturalSortIfEligible(a.getName(), b.getName());
			if (natural != 0)
				return natural;

			if (a.getName().length() != b.getName().length()) {
				SystemLog.warn("ATTENTION! Rotated log files with different file name lengths were selected: " + a.getName() + " <-> " + b.getName()
				+ " This might indicate that the order is wrong or that your filter is not sufficiently restrictive.");
			}
			return a.getName().compareTo(b.getName());
		});
		return files;
	}

	private boolean isCurrentFile(File file, ILogSource source) {
		return file.getName().split("\\.").length == 2;
	}

	private int naturalSortIfEligible(String a, String b) {
		Matcher matchA = naturalSortEligible.matcher(a);
		if (matchA.matches()) {
			Matcher matchB = naturalSortEligible.matcher(b);
			if (matchB.matches()) {
				if (matchA.group(1).equals(matchB.group(1)))
					return Integer.parseInt(matchA.group(2)) - Integer.parseInt(matchB.group(2));
			}
		}
		return 0;
	}

	@Override
	public Path getPath() {
		return path;
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
