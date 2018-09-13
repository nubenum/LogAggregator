package de.nubenum.app.plugin.logaggregator.parts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import de.nubenum.app.plugin.logaggregator.config.ILogHost;
import de.nubenum.app.plugin.logaggregator.config.ILogSource;
import de.nubenum.app.plugin.logaggregator.core.IFileRange;
import de.nubenum.app.plugin.logaggregator.core.layers.IEntryLog;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageLink;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageMatch;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.Level;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.StackedEntry;

public class GuiEntry implements IEntry {
	private static final IEntryMatcher stackMatcher = new EntryMatcher(Level.ALL, "", StackedEntry.class);
	private TextStyle matchStyle = null;
	private TextStyle linkStyle = null;

	private IEntry entry;
	private Display display = Display.getCurrent();

	public GuiEntry(IEntry entry) {
		this.entry = entry;
	}

	private TextStyle getMatchStyle() {
		if (matchStyle == null) {
			matchStyle = new TextStyle();
			matchStyle.background = display.getSystemColor(SWT.COLOR_YELLOW);
		}
		return matchStyle;
	}

	private TextStyle getLinkStyle() {
		if (linkStyle == null) {
			linkStyle = new TextStyle();
			linkStyle.underline = true;
			linkStyle.underlineStyle = SWT.UNDERLINE_LINK;
		}
		return linkStyle;
	}

	public IEntry getEntry() {
		return entry;
	}

	public String getHostString() {
		if (entry.getHost() == null) return "";
		return entry.getHost().getShortName();
	}

	public String getPathString() {
		if (entry == null || entry.getHost() == null)
			return "";
		String path = entry.getPath();
		if (path != null)
			return path;
		return "";
	}

	public String getLogTimeString() {
		LogTime actual = entry.getActualLogTime();
		if (actual != null)
			return actual.toString();
		if (entry.getLogTime() == null) return "";
		return entry.getLogTime().toString();
	}

	public Color getLogTimeColor() {
		LogTime actual = entry.getActualLogTime();
		if (actual != null)
			return display.getSystemColor(SWT.COLOR_RED);
		return null;
	}

	public String getLevelString() {
		String count = "";
		int size = entry.getChildren().size();
		if (size > 0) {
			if (size > IEntryLog.TRUNCATE_GROUP_SIZE)
				count = " (10000+)";
			else
				count = " ("+entry.getChildren().size()+")";
		}
		String lvl = "NONE";
		if (entry.getLevel() != null && entry.getLogTime() != null) {
			lvl = entry.getLevel().toString();
		}
		return lvl + count;
	}

	public Color getLevelColor() {
		int color = SWT.COLOR_BLACK;

		if (entry.getLevel() != null) {
			int ord = entry.getLevel().ordinal();
			if (ord <= Level.ERROR.ordinal())
				color = SWT.COLOR_RED;
			else if (ord <= Level.WARNING.ordinal())
				color = SWT.COLOR_DARK_YELLOW;
		}

		return display.getSystemColor(color);
	}

	@Override
	public String getMessageComplete() {
		if (entry == null)
			return "";
		return entry.getMessageComplete();
	}

	public StyleRange[] getMessageCompleteStyleRanges(IEntryMatcher matcher) {
		if (entry == null)
			return new StyleRange[] {};
		List<StyleRange> ranges = entryMessageMatchesToStyleRanges(entry.getLinks(), getLinkStyle(), true);
		ranges.addAll(entryMessageMatchesToStyleRanges(entry.getMatches(matcher, true), getMatchStyle(), false));

		ranges.sort((StyleRange a, StyleRange b) -> {
			return a.start-b.start;
		});

		ranges = fixOverlappingRanges(ranges);
		StyleRange[] arr = new StyleRange[ranges.size()];
		return ranges.toArray(arr);
	}

	private List<StyleRange> entryMessageMatchesToStyleRanges(List<? extends EntryMessageMatch> matches, TextStyle style, boolean data) {
		List<StyleRange> ranges = new ArrayList<>();

		for (EntryMessageMatch match : matches) {
			StyleRange range = new StyleRange(style);
			range.start = match.getStart();
			range.length = match.getEnd() - match.getStart();
			if (data) range.data = match;
			ranges.add(range);
		}

		return ranges;
	}

	private List<StyleRange> fixOverlappingRanges(List<StyleRange> ranges) {
		StyleRange lastRange = null;
		Iterator<StyleRange> it = ranges.iterator();
		while (it.hasNext()) {
			StyleRange range = it.next();
			if (lastRange != null && lastRange.start+lastRange.length > range.start) {
				lastRange.length = range.start-lastRange.start;
			}
			lastRange = range;
		}
		return ranges;
	}

	public StyledString getMessageStyled(IEntryMatcher matcher) {
		StyledString text = new StyledString(entry.getMessage());
		List<EntryMessageMatch> list = entry.getMatches(matcher, false);

		Styler styler = StyledString.COUNTER_STYLER;

		for (EntryMessageMatch match: list) {
			text.setStyle(match.getStart(), match.getEnd()-match.getStart(), styler);
		}
		return text;
	}

	public Color getMessageColor() {
		int color = SWT.COLOR_BLACK;
		if (entry.matches(stackMatcher))
			color = SWT.COLOR_RED;
		return display.getSystemColor(color);
	}

	@Override
	public List<IEntry> getChildren() {
		return entry.getChildren();
	}

	@Override
	public boolean matches(IEntryMatcher filter) {
		return entry.matches(filter);
	}

	@Override
	public LogTime getLogTime() {
		return entry.getLogTime();
	}

	@Override
	public LogTime getActualLogTime() {
		return entry.getActualLogTime();
	}

	@Override
	public String getMessage() {
		return entry.getMessage();
	}

	@Override
	public Level getLevel() {
		return entry.getLevel();
	}

	@Override
	public String getPath() {
		return entry.getPath();
	}

	@Override
	public IFileRange getRange() {
		return entry.getRange();
	}

	@Override
	public ILogHost getHost() {
		return entry.getHost();
	}

	@Override
	public ILogSource getSource() {
		return entry.getSource();
	}

	@Override
	public int compareTo(IEntry o) {
		return entry.compareTo(o);
	}

	@Override
	public List<EntryMessageMatch> getMatches(IEntryMatcher matcher, boolean complete) {
		return entry.getMatches(matcher, complete);
	}

	@Override
	public List<EntryMessageLink> getLinks() {
		return entry.getLinks();
	}
}
