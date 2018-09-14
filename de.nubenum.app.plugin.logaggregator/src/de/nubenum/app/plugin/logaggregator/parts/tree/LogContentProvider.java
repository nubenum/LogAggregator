package de.nubenum.app.plugin.logaggregator.parts.tree;

import java.io.IOException;

import org.eclipse.jface.viewers.ILazyContentProvider;

import de.nubenum.app.plugin.logaggregator.core.AsyncCompletableQueue;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.layers.IFilteredLog;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.Entry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;
import de.nubenum.app.plugin.logaggregator.parts.DefaultConstants;
import de.nubenum.app.plugin.logaggregator.parts.GuiEntry;

public class LogContentProvider implements ILazyContentProvider {
	private static final int VIRTUAL_SIZE = 10000;
	private final LogTreeViewer viewer;
	private final IFilteredLog log;
	private ReferenceOffset lastUpdated = null;
	private ReferenceOffset lastSelected = null;

	private AsyncCompletableQueue queue;

	public LogContentProvider(LogTreeViewer viewer, IFilteredLog log) {
		this.viewer = viewer;
		this.log = log;
		this.queue = new AsyncCompletableQueue();
		this.queue.addListener(e -> {
			viewer.onUpdate(e);
		});
		viewer.setItemCount(VIRTUAL_SIZE);
	}

	private Direction getRoughDirection(int index) {
		if (index < VIRTUAL_SIZE/2) return Direction.UP;
		return Direction.DOWN;
	}

	private int getBoundIndex(Direction dir) {
		return dir == Direction.UP ? 0 : VIRTUAL_SIZE-1;
	}

	private IEntry getLastUpdatedEntry(int index) {
		if (lastUpdated == null) {
			if (getRoughDirection(index) == Direction.UP) return Entry.FIRST;
			return Entry.LAST;
		}
		return lastUpdated.getEntry();
	}

	private int getLastUpdatedIndexDiff(int index) {
		if (lastUpdated == null) {
			if (getRoughDirection(index) == Direction.UP) return index+1;
			return index-VIRTUAL_SIZE;
		}
		return index-lastUpdated.getOffset();
	}

	@Override
	public void updateElement(int index) {
		System.out.println(index);
		ReferenceOffset viewportTop = viewer.getViewportTop();
		ReferenceOffset viewportBottom = viewer.getViewportBottom();
		if (!isInRoughViewportRange(index, viewportTop, viewportBottom)) {
			System.out.println("eager index request filtered: "+index);
			return;
		}
		viewer.onUpdate(new UpdateEvent(Event.START));
		queue.addToQueue(() -> {
			doUpdateElement(index, viewportTop, viewportBottom);
		});
	}

	private synchronized void doUpdateElement(int index, ReferenceOffset viewportTop, ReferenceOffset viewportBottom) {
		try {
			int offset = getLastUpdatedIndexDiff(index);
			IEntry entry = log.getAt(getLastUpdatedEntry(index), offset);
			if (entry == null) {
				doUpdateGuiElement(DefaultConstants.LOAD_ENTRY, index);
				viewer.onUpdate(new UpdateEvent(Event.STOP));
				return;
			} else if (Entry.isFirstOrLast(entry)) {
				System.out.println("GUIEND:"+index +"|"+entry);
				Direction atBounds = atViewportBounds(viewportTop, viewportBottom);
				if (atBounds == Direction.NONE || atBounds == Direction.get(entry)) {
					guiRun(() -> {
						int itemToShow = getBoundIndex(Direction.get(entry));
						resetPosition();
						viewer.setTopItem(itemToShow);
					});
				}
				return;
			}
			if (Thread.interrupted())
				return;

			doUpdateGuiElement(entry, index);
			updateLastReferences(entry, index);

			if (isNotEndReachedAtViewportBounds(entry, index, viewportTop, viewportBottom)) {
				//viewportTop would be better for seamless scroll down, but less stable
				guiRun(() -> switchPage(lastUpdated.getEntry(), lastUpdated.getOffset()));
				return;
			}
		} catch (Throwable e) {
			SystemLog.log(e);
			e.printStackTrace();
		}
	}

	private synchronized void doUpdateGuiElement(IEntry entry, int index) {
		guiRun(() -> {
			System.out.println(index+"|"+entry.getLogTime());
			viewer.replace(new GuiEntry(entry), index);
		});
	}

	private synchronized void updateLastReferences(IEntry entry, int index) {
		lastUpdated = new ReferenceOffset(entry, index);
		/*if (lastSelected != null && lastSelected.getEntry() == null && lastSelected.getOffset() == index) {
			lastSelected = lastUpdated;
			viewer.onUpdate(new UpdateEvent(new GuiEntry(entry)));
		}*/
	}

	private boolean isInRoughViewportRange(int index, ReferenceOffset viewportTop, ReferenceOffset viewportBottom) {
		int threshold = VIRTUAL_SIZE/100;

		int topOffset = viewportTop.getOffset();
		int bottomOffset = viewportBottom.getOffset();

		if (index < topOffset-threshold || index > bottomOffset+threshold)
			return false;
		return true;
	}

	private boolean isNotEndReachedAtViewportBounds(IEntry next, int index, ReferenceOffset viewportTop, ReferenceOffset viewportBottom) throws IOException {
		if (atViewportBounds(viewportTop, viewportBottom) != Direction.NONE) {
			Direction dir = getRoughDirection(index);
			IEntry entry = log.getAt(lastUpdated.getEntry(), getLastUpdatedIndexDiff(getBoundIndex(dir)+dir.getValue()));
			if (!Entry.isFirstOrLast(entry))
				return true;
		}
		return false;
	}

	private Direction atViewportBounds(ReferenceOffset viewportTop, ReferenceOffset viewportBottom) {
		if (viewportBottom.getOffset() >= VIRTUAL_SIZE-1)
			return Direction.DOWN;
		if (viewportTop.getOffset() <= 0)
			return Direction.UP;
		return Direction.NONE;
	}

	public void resetPosition() {
		setPosition(null, -1);
		lastUpdated = null;
	}

	private void setPosition(IEntry entry, int index) {
		queue.stop();
		lastUpdated = new ReferenceOffset(entry, index);
		lastSelected = null;
	}

	public void resetRetriever() {
		queue.stop();
	}

	public void scrollToBottom() {
		resetPosition();
		viewer.setTopItem(VIRTUAL_SIZE-1);
	}

	public void jumpToDate(LogTime ts) {
		setPosition(new LinedEntry(ts), VIRTUAL_SIZE/2);
		viewer.setTopItem(VIRTUAL_SIZE/2+1);
	}

	public void jumpToMatch(Direction dir) {
		queue.addToQueue(() -> {
			ReferenceOffset ref = lastSelected;
			if (ref == null || ref.getEntry() == null)
				ref = lastUpdated;
			if (ref == null)
				return;

			try {
				ReferenceOffset result = log.getMatchingAt(ref.getEntry(), dir.getValue());
				if (Entry.isFirstOrLast(result.getEntry()))
					return;
				final int index = ref.getOffset()+result.getOffset();
				if (index >= 0 && index < VIRTUAL_SIZE) {
					lastSelected = new ReferenceOffset(result.getEntry(), index);
					guiRun(() -> viewer.selectAndShowItem(index));
				} else {
					lastSelected = new ReferenceOffset(result.getEntry(), VIRTUAL_SIZE/2);
					guiRun(() -> switchPage(result.getEntry(), lastUpdated.getOffset()));
				}
			} catch (IOException e) {
				SystemLog.log(e);
				e.printStackTrace();
			}
		});
	}

	public void switchPage(IEntry next, int index) {
		IEntry entry = next;
		if (entry == null)
			entry = lastUpdated.getEntry();
		System.out.println("switch page pivot "+ entry);
		setPosition(entry, VIRTUAL_SIZE/2);
		viewer.setTopItem(VIRTUAL_SIZE/2);
	}

	public boolean isAtBottom() {
		ReferenceOffset bottom = viewer.getViewportBottom();
		if (bottom.getOffset() >= VIRTUAL_SIZE-1) {
			return true;
		}
		return false;
	}

	public void append() {
		if (isAtBottom()) {
			ReferenceOffset up = viewer.getViewportTop();
			switchPage(up.getEntry(), up.getOffset());
		}
	}

	public void setSelected(ReferenceOffset selected) {
		lastSelected = selected;
	}

	private void guiRun(Runnable runnable) {
		if (viewer == null)
			return;
		viewer.getDisplay().syncExec(runnable);
	}

	@Override
	public void dispose() {
		System.out.println("dispose cprov");
		queue.stop();
	}
}
