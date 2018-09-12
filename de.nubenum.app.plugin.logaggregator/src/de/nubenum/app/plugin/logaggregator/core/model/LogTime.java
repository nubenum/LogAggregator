package de.nubenum.app.plugin.logaggregator.core.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Representing the comparable timestamp of a log entry. More supported
 * timestamp formats can be added. In this case, the timeExtractor pattern must
 * be amended, and new entries added to the timeDetectors and timeFormats.
 *
 */
public class LogTime implements Comparable<LogTime> {
	/**
	 * A virtual LogTime that is always the earliest.
	 */
	public static final LogTime MIN = new LogTime();
	/**
	 * A virtual LogTime that is always the latest.
	 */
	public static final LogTime MAX = new LogTime();
	/**
	 * A virtual LogTime that is undefined.
	 */
	public static final LogTime NONE = new LogTime();
	/**
	 * The default format to print out timestamps.
	 */
	public static final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private static final Pattern[] timeDetectors = {
			Pattern.compile(".*\\/.*"),
			Pattern.compile("[0-9-]+ ([0-9]+:){3}[0-9]{3}"),
			Pattern.compile("[0-9-]+ ([0-9]+[:.]){3}[0-9]{3}"),
			Pattern.compile("[0-9-]+ ([0-9]+:){2}[0-9]{2}"),
			Pattern.compile("[0-9]+"),
			Pattern.compile("[0-9: A-Z-]+")
	};
	private static final DateTimeFormatter[] timeFormats = {
			DateTimeFormatter.ofPattern("M/d/yy H:m:s:SSS zzz", Locale.US),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS", Locale.US),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
			null, // Unix time stamp
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS zzz", Locale.US)
	};

	private static final String timeExtractor = "(?:\\[?[0-9/-]+ [0-9:.]+(?: [A-Z]+)?\\]?|[0-9]{13,})[ ;]";

	/**
	 * @return The regex pattern used to extract timestamps from arbitrary strings.
	 */
	public static String getTimeExtractor() {
		return timeExtractor;
	}

	private LocalDateTime time;

	private LogTime() {
		time = null;
	}

	/**
	 * Parse the given timestamp and instantiate the respective LogTime
	 *
	 * @param line
	 *            A string representing a timestamp as obtained by matching against
	 *            {@link #getTimeExtractor()}.
	 * @throws DateTimeParseException
	 *             If the timestamp could not be parsed
	 */
	public LogTime(String line) throws DateTimeParseException {
		line = line.replaceAll("[^A-Z0-9-/:. ]", "").trim();
		for (int i = 0; i < timeDetectors.length; i++) {
			if (timeDetectors[i].matcher(line).matches()) {
				if (timeFormats[i] == null)
					time = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(line)),
							ZoneOffset.systemDefault());
				else
					time = LocalDateTime.parse(line, timeFormats[i]);
				return;
			}
		}
		throw new DateTimeParseException("No matching DateTime format found", line, 0);
	}

	/**
	 * Init a LogTime from the given parameters, {@see LocalDateTime#of(int, int,
	 * int, int, int, int, int)}
	 *
	 * @param year
	 * @param month
	 * @param dayOfMonth
	 * @param hour
	 * @param minute
	 * @param second
	 * @param millisecond
	 */
	public LogTime(int year, int month, int dayOfMonth, int hour, int minute, int second, int millisecond) {
		time = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, millisecond * 1000000);
	}

	@Override
	public int compareTo(LogTime other) {
		if (this == other)
			return 0;
		if (this == NONE || other == NONE)
			return 0;
		if (this == MIN || other == MAX)
			return -1;
		if (this == MAX || other == MIN)
			return 1;
		return time.compareTo(other.time);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LogTime))
			return false;
		if (this == other)
			return true;
		if (time == null)
			return false;
		return time.equals(((LogTime) other).time);
	}

	@Override
	public int hashCode() {
		if (time == null)
			return 0;
		return time.hashCode();
	}

	@Override
	public String toString() {
		if (this == MIN)
			return "MIN";
		if (this == MAX)
			return "MAX";
		if (this == NONE)
			return "NONE";
		return time.format(outputFormat);
	}
}
