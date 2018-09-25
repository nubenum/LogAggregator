package de.nubenum.app.plugin.logaggregator.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;

/**
 * Implementing the ILogDirectory with generic startsWith filtering and sorting.
 * This class should be concretized for different storage backends.
 *
 */
public abstract class AbstractLogDirectory implements ILogDirectory {
	private static final Pattern naturalSortEligible = Pattern.compile("(.*?)(\\d+)");
	protected Path path;
	protected ILogHost host;

	public AbstractLogDirectory(Path location, ILogHost host, ILogSource source) {
		this.host = host;
		this.path = location.resolve(host.getName());
		Path sourceParent = Paths.get(source.getName()).getParent();
		if (sourceParent != null)
			this.path = this.path.resolve(sourceParent);
	}

	/**
	 * Get all files from this directory
	 *
	 * @return A List of all files in that directory
	 * @throws IOException
	 *             If the directory is unavailable or no files were found
	 */
	protected abstract List<File> getAllFiles() throws IOException;

	@Override
	public List<File> getSourceFiles(ILogSource source) throws IOException {
		List<File> files = getAllFiles();

		String match = Paths.get(source.getName()).getFileName().toString();
		files = files.stream().filter(f -> f.getName().startsWith(match)).collect(Collectors.toList());

		Collection<File> lengthErrors = new ArrayList<>();
		files.sort((a, b) -> {
			if (isCurrentFile(a, source))
				return 1;
			if (isCurrentFile(b, source))
				return -1;

			int natural = naturalSortIfEligible(a.getName(), b.getName());
			if (natural != 0)
				return natural;

			if (a.getName().length() != b.getName().length() && !lengthErrors.contains(a) && !lengthErrors.contains(b)) {
				SystemLog.warn("ATTENTION! Irregularities in rotated log file naming were detected: " + a.getName()
				+ " <-> " + b.getName()
				+ " This might indicate that the order is wrong or that your filter is not sufficiently restrictive and might lead to endless loops.");
				lengthErrors.add(a);
				lengthErrors.add(b);
			}
			return a.getName().compareTo(b.getName());
		});
		return files;
	}

	private boolean isCurrentFile(File file, ILogSource source) {
		return file.getName().split("\\.").length <= 2;
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
	public boolean equals(Object other) {
		return EqualsHelper.objectsEqual(AbstractLogDirectory.class, this, other, d -> d.getPath());
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}
