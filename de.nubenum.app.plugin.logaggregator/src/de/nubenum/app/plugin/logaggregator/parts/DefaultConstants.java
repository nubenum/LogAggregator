package de.nubenum.app.plugin.logaggregator.parts;

import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;

public class DefaultConstants {
	public static final String VIRTUAL_ACTION = "de.nubenum.app.plugin.logaggregator.virtualaction";
	public static final String VIRTUAL_ACTION_CREATE_DEFAULT = "createDefault";

	private static final String LOAD_ENTRY_TEXT = "Error while loading config";
	public static final IEntry LOAD_ENTRY = new LinedEntry(LOAD_ENTRY_TEXT);

	public static final String DEFAULT_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<!-- A LogAggregator config file might look like this. It needs to have the \".logagg\" file extension. -->\n"
			+ "<aggregatorConfig>\n" + "	<!-- The location of the directory holding all log files. -->\n"
			+ "	<location>C:/logs/</location>\n" + "	<hosts>\n"
			+ "		<!-- Subdirectories that contain similar types of logs, e.g. the same logs from different hosts. -->\n"
			+ "		<host>host1/</host>\n" + "		<!-- Define a short name to be displayed in the UI. -->\n"
			+ "		<host shortName=\"alias\">host2/</host>\n" + "	</hosts>\n" + "	<sources>\n"
			+ "		<!-- Actual log files that are contained in each of the host directories. Files are matched using startsWith.\n"
			+ "		Thus, the file extension can be omitted to fetch all matching rotated log files. -->\n"
			+ "		<source>error</source>\n"
			+ "		<!-- In case some logs are not available on all hosts, use the ignoreNotFound attribute. The default, if omitted, is false. -->\n"
			+ "		<source ignoreNotFound=\"true\">subdirectory/stdout</source>\n"
			+ "		<source ignoreNotFound=\"false\">access</source>\n" + "	</sources>\n"
			+ "	<!-- With the above config, the following files might be pulled in:\n" + "	C:/logs/host1/error.log.5\n"
			+ "	C:/logs/host1/error.log.6\n" + "	C:/logs/host1/error.log\n"
			+ "	C:/logs/host1/subdirectory/stdout.log\n" + "	C:/logs/host1/access.log\n"
			+ "	C:/logs/host2/error.log\n" + "	C:/logs/host2/access_18.01.01.log\n" + "	C:/logs/host2/access.log\n"
			+ "	-->\n" + "</aggregatorConfig>\n";
}
