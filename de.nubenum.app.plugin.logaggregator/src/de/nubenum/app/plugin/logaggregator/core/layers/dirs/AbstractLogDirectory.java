package de.nubenum.app.plugin.logaggregator.core.layers.dirs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.Utils;
import de.nubenum.app.plugin.logaggregator.core.config.ILogSource;

/**
 * Implementing the ILogDirectory with generic startsWith filtering and sorting.
 * This class should be concretized for different storage backends.
 *
 */
public abstract class AbstractLogDirectory implements ILogDirectory {
	private static final Pattern naturalSortEligible = Pattern.compile("(.*?)(\\d+)");


	public AbstractLogDirectory() {
		return;
	}



	/**
	 * Get all files from this directory
	 *
	 * @return A List of all files in that directory
	 * @throws IOException
	 *             If the directory is unavailable or no files were found
	 */
	protected abstract List<URI> getAllFiles() throws IOException;

	@Override
	public List<URI> getSourceFiles(ILogSource source) throws IOException {
		List<URI> files = getAllFiles();

		String match = Paths.get(source.getName()).getFileName().toString();
		files = files.stream().filter(f -> Utils.getFileName(f).startsWith(match)).collect(Collectors.toList());

		Collection<URI> lengthErrors = new ArrayList<>();
		files.sort((a, b) -> {
			if (isCurrentFile(a, source))
				return 1;
			if (isCurrentFile(b, source))
				return -1;

			int natural = naturalSortIfEligible(Utils.getFileName(a), Utils.getFileName(b));
			if (natural != 0)
				return natural;

			if (Utils.getFileName(a).length() != Utils.getFileName(b).length() && !lengthErrors.contains(a) && !lengthErrors.contains(b)) {
				SystemLog.warn("ATTENTION! Irregularities in rotated log file naming were detected: " + Utils.getFileName(a)
				+ " <-> " + Utils.getFileName(b)
				+ " This might indicate that the order is wrong or that your filter is not sufficiently restrictive and might lead to endless loops.");
				lengthErrors.add(a);
				lengthErrors.add(b);
			}
			return Utils.getFileName(a).compareTo(Utils.getFileName(b));
		});
		return files;
	}

	private boolean isCurrentFile(URI file, ILogSource source) {
		//TODO more robust detection
		return Utils.getFileName(file).toString().split("\\.").length <= 2;
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
}
