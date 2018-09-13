package de.nubenum.app.plugin.logaggregator.core.model;

import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.FileRange;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;

/**
 * An entry that represents one line in a log.
 */
public class LinedEntry extends Entry {
	private static Pattern pattern;
	private IFileRange range = null;
	private String message = null;
	private String parsedPart = null;
	private Level level = null;
	private ILogHost host = null;
	private ILogSource source = null;
	private LogTime time = null;
	private LogTime actualTime = null;

	public LinedEntry() {
		return;
	}

	public LinedEntry(String line, IFileRange range, ILogHost host, ILogSource source) {
		this.range = range;
		this.host = host;
		this.source = source;
		parseLine(line);
	}

	public LinedEntry(String line) {
		this(line, new FileRange(0, line.length()), null, null);
	}

	public LinedEntry(LogTime time) {
		this.time = time;
	}

	private static Pattern getPattern() {
		if (pattern == null) {
			String timePattern = LogTime.getTimeExtractor();
			pattern = Pattern.compile("(?<ts>"+timePattern+")?(?<lvl>\\[[l:A-Z]+\\] )?(?<msg>.*)", Pattern.DOTALL);
		}
		return pattern;
	}

	private boolean isLineWithoutProperties(String line) {
		if (getRange().getLength() == 0 || line.charAt(0) == ' ')
			return true;
		return false;
	}

	private void parseLine(String line) {
		this.message = line;
		if (isLineWithoutProperties(line))
			return;
		Matcher matcher = getPattern().matcher(line);
		if (matcher.matches()) {
			try {
				String msg = matcher.group("msg");
				String ts = matcher.group("ts");
				String lvl = matcher.group("lvl");
				if (ts != null)
					this.time = new LogTime(ts);

				if (msg != null) {
					this.message = msg;
					this.parsedPart = (ts != null ? ts : "") + (lvl != null ? lvl : "");
				}
			} catch (DateTimeParseException e) {
				SystemLog.log(e);
			}
			this.level = parseLevel(matcher.group("lvl"));
		}
	}

	private Level parseLevel(String level) {
		if (level == null || level.length() == 0)
			return null;
		level = level.replaceAll("[^A-Z]", "");
		Level[] levels = Level.values();
		for(Level l : levels) {
			if (l.name().equals(level))
				return l;
		}
		return Level.OTHER;
	}

	@Override
	public IFileRange getRange() {
		return range;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getMessageComplete() {
		return parsedPart+message;
	}

	@Override
	public List<IEntry> getChildren() {
		return new ArrayList<IEntry>();
	}

	@Override
	public LogTime getLogTime() {
		return time;
	}

	@Override
	public LogTime getActualLogTime() {
		return this.actualTime;
	}

	public void setLogTime(LogTime time) {
		if (this.time != null) {
			this.actualTime = this.time;
		}
		this.time = time;
	}

	@Override
	public Level getLevel() {
		return level;
	}

	@Override
	public ILogHost getHost() {
		return host;
	}

	@Override
	public ILogSource getSource() {
		return source;
	}

	@Override
	public String getPath() {
		return Paths.get(getHost().getName(), getSource().getName()).toString();
	}
}
