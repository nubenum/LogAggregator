package de.nubenum.app.plugin.logaggregator.gui.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.nubenum.app.plugin.logaggregator.core.IUpdateInitiator;
import de.nubenum.app.plugin.logaggregator.core.IUpdateListener;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent;
import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.ReferenceOffset;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.gui.GuiEntry;
import de.nubenum.app.plugin.logaggregator.gui.LogController;

public class LogTreeViewer implements IUpdateInitiator, IUpdateListener {

	private LogContentProvider provider;
	private TableViewer table;
	private LogController control;
	private List<IUpdateListener> listeners = new ArrayList<>();

	public LogTreeViewer(Composite parent, LogController control) {
		this.control = control;
		control.addListener(e -> {
			if (e.getType() == Event.STOP)
				getDisplay().syncExec(() -> stopAndRefresh());
		});

		this.table = new TableViewer(parent, SWT.VIRTUAL | SWT.FULL_SELECTION);
		table.getTable().setHeaderVisible(true);
		table.setUseHashlookup(true);

		this.provider = new LogContentProvider(this, control.getLog());
		table.setContentProvider(provider);

		createTableColumns(control);

		ColumnViewerToolTipSupport.enableFor(table);

		table.addSelectionChangedListener(evt -> {
			IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
			GuiEntry entry = (GuiEntry) sel.getFirstElement();
			if (entry != null) {
				provider.setSelected(new ReferenceOffset(entry.getEntry(), table.getTable().getSelectionIndex()));
				listeners.forEach(l -> l.onUpdate(new UpdateEvent(entry)));
			} else {
				listeners.forEach(l -> l.onUpdate(new UpdateEvent(new GuiEntry(null))));
			}
		});

		scrollToBottom();
	}

	private void createTableColumns(LogController control) {
		TableViewerColumn timeColumn = new TableViewerColumn(table, SWT.NONE);
		timeColumn.getColumn().setWidth(150);
		timeColumn.getColumn().setText("Time");
		timeColumn.getColumn().setToolTipText("This will be highlighted red if badly ordered timestamps were detected and \"spoofed\". The tooltip will give you the raw timestamp and level as it was read from the file.");
		timeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object o) {
				return ((GuiEntry) o).getLogTimeString();
			}
			@Override
			public String getToolTipText(Object o) {
				return ((GuiEntry) o).getParsedPart();
			}
			@Override
			public Color getForeground(Object o) {
				return ((GuiEntry) o).getLogTimeColor();
			}
		});

		TableViewerColumn hostColumn = new TableViewerColumn(table, SWT.NONE);
		hostColumn.getColumn().setWidth(70);
		hostColumn.getColumn().setText("Host");
		hostColumn.getColumn().setToolTipText("The host from which the respective entry originates. The tooltip will give you the exact log file.");
		hostColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object o) {
				return ((GuiEntry) o).getHostString();
			}
			@Override
			public String getToolTipText(Object o) {
				if (o == null)
					return "";
				return ((GuiEntry) o).getPathString();
			}
			@Override
			public Font getFont(Object o) {
				return JFaceResources.getFont(JFaceResources.TEXT_FONT);
			}
		});

		TableViewerColumn levelColumn = new TableViewerColumn(table, SWT.NONE);
		levelColumn.getColumn().setWidth(100);
		levelColumn.getColumn().setText("Level (Count)");
		levelColumn.getColumn().setToolTipText("The log level of the entry or NONE if no level was specified, and the amount of children this entry has (this can either be the number of lines for stacktraces or the number of duplicates for deduplicated entries). This will be highlighted red for ERROR and higher and yellowish for WARNING and higher.");
		levelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object o) {
				return ((GuiEntry) o).getLevelString();
			}
			@Override
			public String getToolTipText(Object o) {
				return ((GuiEntry) o).getParsedPart();
			}
			@Override
			public Color getForeground(Object o) {
				return ((GuiEntry) o).getLevelColor();
			}
		});

		TableViewerColumn msgColumn = new TableViewerColumn(table, SWT.NONE);
		msgColumn.getColumn().setWidth(3000);
		msgColumn.getColumn().setText("Message");
		msgColumn.getColumn().setToolTipText("The log message. Stacktraces will be highlighted red.");
		msgColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new StyledLogLabelProvider(control)));
	}

	public void stopAndRefresh() {
		System.out.println("refersh");
		provider.resetRetriever();
		table.refresh();
	}

	public void scrollToBottom() {
		provider.scrollToBottom();
	}

	public void jumpToDate(LogTime ts) {
		provider.jumpToDate(ts);
	}

	public void jumpToMatch(IEntryMatcher matcher, Direction dir) {
		control.getLog().setMatcher(matcher);
		control.getLog().toggleFilter(false);
		provider.jumpToMatch(dir);
	}

	public void applyFilter(IEntryMatcher matcher) {
		control.getLog().setMatcher(matcher);
		control.getLog().toggleFilter(true);
		stopAndRefresh();
	}

	public void selectAndShowItem(int index) {
		table.getTable().deselectAll();
		table.getTable().showItem(table.getTable().getItem(index));
		table.refresh();
		//TODO replace with problematic TableViewer selection?, update detail
		table.getTable().setFocus();
		table.getTable().select(index);
	}

	public void setTopItem(int index) {
		//table.getTable().deselectAll();
		table.getTable().select(index);
		table.getTable().setTopIndex(index);
		//table.getTable().setFocus();
		table.getTable().clearAll();
	}

	public void append() {
		provider.append();
	}

	public void refreshOnBottom() {
		if (provider.isAtBottom()) {
			control.setConfigFile(null);
		}
	}

	public void replace(GuiEntry guiEntry, int index) {
		table.replace(guiEntry, index);
	}

	public void setItemCount(int virtualSize) {
		table.setItemCount(virtualSize);
	}

	public ReferenceOffset getViewportTop() {
		int index = table.getTable().getTopIndex();
		return new ReferenceOffset(getEntry(index), index);
	}

	public ReferenceOffset getViewportBottom() {
		int index = table.getTable().getTopIndex();
		int visibleItemCount = table.getTable().getClientArea().height / table.getTable().getItemHeight();
		index += visibleItemCount;
		index = Math.min(index, table.getTable().getItemCount()-1);
		return new ReferenceOffset(getEntry(index), index);
	}

	private IEntry getEntry(int index) {
		GuiEntry guiEntry = (GuiEntry) table.getTable().getItem(index).getData();
		if (guiEntry == null)
			return null;
		return guiEntry.getEntry();
	}

	public Display getDisplay() {
		return table.getTable().getDisplay();
	}

	@Override
	public void onUpdate(UpdateEvent event) {
		listeners.forEach(l -> l.onUpdate(event));
	}

	@Override
	public void addListener(IUpdateListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(IUpdateListener listener) {
		this.listeners.remove(listener);
	}

}
