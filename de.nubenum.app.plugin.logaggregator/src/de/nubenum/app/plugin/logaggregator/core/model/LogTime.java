package de.nubenum.app.plugin.logaggregator.core.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

	private static final String[] timeFormats = {
			"M/d/yy H:m:s.SSS zzz",
			"M/d/yy H:m:s:SSS zzz",
			"M/d/yy H:m:s.SSS",
			"M/d/yy H:m:s:SSS",
			"M/d/yy H:m:s",
			"yyyy-MM-dd HH:mm:ss.SSS zzz",
			"yyyy-MM-dd HH:mm:ss:SSS zzz",
			"yyyy-MM-dd HH:mm:ss.SSS",
			"yyyy-MM-dd HH:mm:ss:SSS",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-M-d H:m:s",
			"dd-MM-yyyy HH:mm:ss.SSS zzz",
			"dd-MM-yyyy HH:mm:ss:SSS zzz",
			"dd-MM-yyyy HH:mm:ss.SSS",
			"dd-MM-yyyy HH:mm:ss:SSS",
			"dd-MM-yyyy HH:mm:ss"
	};

	private static DateTimeFormatter[] timeFormatters;
	private static Pattern[] timePatterns;
	private static String timeExtractor;

	private static String getControlCharMatcher(int num) {
		String controlChars = "MdyHmsS";
		return "(["+controlChars+"]{"+num+"})";
	}

	private static String replaceControlChars(String format) {
		return format.replace(".", "\\.")
				.replaceAll(getControlCharMatcher(4), "[0-9]{4}")
				.replaceAll(getControlCharMatcher(3), "[0-9]{3}")
				.replaceAll(getControlCharMatcher(2), "[0-9]{2}")
				.replaceAll(getControlCharMatcher(1), "[0-9]{1,2}")
				.replace("zzz", "[A-Z]+");
	}

	private static void createTimeFormattersPatternsExtractor() {
		if (timeFormatters != null && timePatterns != null && timeExtractor != null)
			return;
		createTimeFormattersPatternsExtractor(timeFormats);
	}

	private static void createTimeFormattersPatternsExtractor(String[] timeFormats) {
		List<DateTimeFormatter> formatters = new ArrayList<>();
		List<Pattern> patterns = new ArrayList<>();
		for (String format : timeFormats) {
			String formatRegex = replaceControlChars(format);
			formatters.add(DateTimeFormatter.ofPattern(format, Locale.US));
			patterns.add(Pattern.compile(formatRegex));
		}
		formatters.add(null); //Unix ts
		patterns.add(Pattern.compile("[0-9]{13}"));

		String extractor = patterns.stream().map(p -> p.toString()).collect(Collectors.joining("|"));
		extractor = "\\[?(?:" + extractor + ")\\]?[ ;]";

		timeFormatters = formatters.toArray(new DateTimeFormatter[formatters.size()]);
		timePatterns = patterns.toArray(new Pattern[patterns.size()]);
		timeExtractor = extractor;
	}

	/**
	 * Set additional log timestamp formats in case the predefined ones are not exhaustive.
	 * @param customLogTimeFormats The List of pattern strings according to {@link DateTimeFormatter}
	 */
	public static void setCustomLogTimeFormats(List<String> customLogTimeFormats) {
		List<String> all = new ArrayList<>();
		all.addAll(Arrays.asList(timeFormats));
		all.addAll(customLogTimeFormats);
		createTimeFormattersPatternsExtractor(all.toArray(new String[all.size()]));
	}

	/**
	 * @return The regex pattern used to extract timestamps from arbitrary strings.
	 */
	public static String getTimeExtractor() {
		createTimeFormattersPatternsExtractor();
		return timeExtractor;
	}

	private LocalDateTime time;

	private LogTime() {
		time = null;
	}

	private LocalDateTime tryFormatter(String input, DateTimeFormatter formatter) {
		if (formatter == null) {
			try {
				return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(input)),
						ZoneOffset.systemDefault());
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			try {
				return LocalDateTime.parse(input, formatter);
			} catch (DateTimeParseException e) {
				return null;
			}
		}
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
	public LogTime(String input) throws DateTimeParseException {
		createTimeFormattersPatternsExtractor();
		String line = input.replaceAll("[^A-Z0-9-/:. ]", "").trim();
		for (int i = 0; i < timePatterns.length; i++) {
			time = tryFormatter(line, timeFormatters[i]);
			if (time != null)
				return;
		}
		throw new DateTimeParseException("No matching DateTime format found for \"" + input + "\"", line, 0);
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
