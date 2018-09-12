package de.nubenum.app.plugin.logaggregator.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Entry implements IEntry {
	/**
	 * A virtual Entry that always represents the very first Entry of all
	 */
	public static final IEntry FIRST = new LinedEntry(LogTime.MIN);
	/**
	 * A virtual Entry that always represents the very last Entry of all
	 */
	public static final IEntry LAST = new LinedEntry(LogTime.MAX);

	private static Pattern linkPattern = Pattern
			.compile("(?<= )(?<pkg>[\\w.]+)\\.[$\\w]+\\.(?<method>[$<>\\w]+)\\((?<cls>\\w+).java:(?<line>\\d+)\\)");

	/**
	 *
	 * @param entry An arbitrary IEntry
	 * @return True if the given entry is the virtual first or last one
	 */
	public static boolean isFirstOrLast(IEntry entry) {
		return entry == FIRST || entry == LAST;
	}

	/**
	 * Get the first or last entry in Direction.
	 *
	 * @param dir
	 *            The Direction
	 * @return FIRST for UP, LAST for DOWN.
	 */
	public static IEntry getFirstOrLast(Direction dir) {
		// TODO Direction.NONE
		if (dir == Direction.UP)
			return Entry.FIRST;
		return Entry.LAST;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Entry) {
			return doCompareTo((Entry) other, false) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int start = (int) (getRange() == null ? 0 : getRange().getTop().getByteOffset() % 1e6);
		int end = (int) (getRange() == null ? 0 : getRange().getBottom().getByteOffset() % 1e6);
		return start + end;
	}

	@Override
	public int compareTo(IEntry other) {
		return doCompareTo(other, true);
	}

	private int doCompareTo(IEntry other, boolean nullEqual) {
		if (this == other)
			return 0;
		if (this == Entry.FIRST || other == Entry.LAST)
			return -1;
		if (this == Entry.LAST || other == Entry.FIRST)
			return 1;
		int time = compareNullable(other, e -> e.getLogTime(), t -> t, nullEqual);
		if (time != 0)
			return time;
		int host = compareNullable(other, e -> e.getHost(), h -> h.getName(), nullEqual);
		if (host != 0)
			return host;
		int source = compareNullable(other, e -> e.getSource(), s -> s.getName(), nullEqual);
		if (source != 0)
			return source;
		int pos = compareNullable(other, e -> e.getRange(), r -> r, nullEqual);
		if (pos != 0)
			return pos;
		return 0;
	}

	private <N, C extends Comparable<C>> int compareNullable(IEntry other, Function<IEntry, N> obtainNullable,
			Function<N, C> obtainComparable, boolean nullEqual) {
		N a = obtainNullable.apply(this);
		N b = obtainNullable.apply(other);
		if (a == null || b == null)
			return nullEqual ? 0 : -1;
		return obtainComparable.apply(a).compareTo(obtainComparable.apply(b));
	}

	/**
	 * Create an enumeration of children properties to build the path for grouped
	 * entries with multiple hosts and/or sources, e.g. <code>{host1,host2}</code>
	 *
	 * @param obtainPathMember
	 *            A Function that will obtain a string representation of the desired
	 *            property for a given child entry
	 * @return A comma separated string enclosed by braces if there is more than one
	 *         child, or just the stringified property of the single child
	 */
	protected String bracedChildrenPath(Function<IEntry, String> obtainPathMember) {
		return getChildren().stream().map(c -> obtainPathMember.apply(c)).collect(Collectors.joining(",", "{", "}"));
	}

	@Override
	public List<EntryMessageMatch> getMatches(IEntryMatcher matcher, boolean complete) {
		List<EntryMessageMatch> list = new ArrayList<>();
		Pattern p = matcher.getMessagePattern();
		if (p != null) {
			Matcher m = matcher.getMessagePattern().matcher(complete ? getMessageComplete() : getMessage());
			while (m.find()) {
				list.add(new EntryMessageMatch(m.start(), m.end()));
			}
		}
		return list;
	}

	@Override
	public List<EntryMessageLink> getLinks() {
		Matcher m = linkPattern.matcher(getMessageComplete());
		List<EntryMessageLink> links = new ArrayList<>();
		while (m.find()) {
			links.add(new EntryMessageLink(m.start("cls"), m.end("line"), m.group("pkg"), m.group("cls"),
					m.group("method"), m.group("line")));
		}
		return links;
	}

	/**
	 * Check whether this entry's Level and message match.
	 * @param matcher The Matcher containing the Level and message pattern.
	 * @return True if this entry matches.
	 */
	protected boolean matchesProperties(IEntryMatcher matcher) {
		if (getLevel() == null && matcher.getMinLevel() != Level.ALL
				|| getLevel() != null && matcher.getMinLevel().compareTo(getLevel()) < 0)
			return false;
		Pattern p = matcher.getMessagePattern();
		if (getMessage() != null && p != null && !p.matcher(getMessageComplete()).find())
			return false;
		return true;
	}

	@Override
	public boolean matches(IEntryMatcher matcher) {
		if (!matcher.getType().isInstance(this))
			return false;
		return matchesProperties(matcher);
	}

	@Override
	public String toString() {
		String host = getHost() != null ? getHost().getShortName() : "";
		return getLogTime() + " " + host + " " + getChildren().size() + " " + getMessage();
	}
}
