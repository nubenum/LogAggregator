package de.nubenum.app.plugin.logaggregator.parts;

import java.io.File;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageLink;
import de.nubenum.app.plugin.logaggregator.core.model.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.Level;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.StackedEntry;
import de.nubenum.app.plugin.logaggregator.parts.tree.LogTreeViewer;

//TODO make as editor
public class LogView {

	private LogController control;
	private File configFile;
	private int countLines = 0;

	private LogTreeViewer viewer;
	private StyledText detail;
	private DateTime date;
	private DateTime time;
	private Text regex;
	private Combo level;
	private Button stack;
	private Label counter;
	private ProgressBar status;
	private Button apply;
	private Button down;
	private Button up;
	private Font bold;

	@Inject UISynchronize sync;

	public LogView() {
		control = new LogController();
		control.addListener(event -> sync.asyncExec(() -> {
			if (event.getType() == Event.COUNT) {
				countLines += event.getCount();
				readLines(countLines);
				counter.pack();
				counter.getParent().pack();
			} else if (event.getType() == Event.EXCEPTION) {
				MessageDialog.openInformation(null, "Error", event.getException().getMessage());
			} else if (event.getType() == Event.APPEND) {
				this.viewer.append();
			} else if (event.getType() == Event.REFRESH) {
				this.viewer.refreshOnBottom();
			} else if (event.getType() == Event.STOP) {
				working(false);
			}
		}));
	}

	@Focus
	public void setFocus() {

	}

	@PreDestroy
	public void destroy() {
		if (bold != null)
			bold.dispose();
		control.close();
	}

	@PostConstruct
	public void createPartControl(Composite container) {
		GridLayout vert = new GridLayout(1, true);
		vert.marginBottom = 0;
		vert.marginWidth = 0;
		container.setLayout(vert);

		createMenuView(container);

		SashForm sash = new SashForm(container, SWT.VERTICAL | SWT.SMOOTH);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createTreeView(sash);
		createDetailView(sash);

		sash.setWeights(new int[] {2, 1});
	}

	private void createMenuView(Composite container) {
		Composite filter = new Composite(container, SWT.NULL);
		RowLayout hor = new RowLayout();
		hor.wrap = true;
		hor.center = true;
		filter.setLayout(hor);
		filter.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		createJumpView(filter);

		separator(filter);

		createSearchView(filter);

		separator(filter);

		createStatusView(filter);
	}

	private void createJumpView(Composite filter) {
		date = new DateTime(filter, SWT.DATE);
		time = new DateTime(filter, SWT.TIME);

		Button jump = new Button(filter, SWT.PUSH);
		jump.setText("Jump");
		jump.setToolTipText("Jump to selected date and time");
		jump.addListener(SWT.Selection, e -> jumpToDate());

		Button toBottom = new Button(filter, SWT.PUSH);
		toBottom.setText("To Bottom");
		toBottom.setToolTipText("Jump to the last entry of all logs");
		toBottom.addListener(SWT.Selection, e -> scrollToBottom());
	}

	private void createSearchView(Composite filter) {
		level = new Combo(filter, SWT.DROP_DOWN);
		level.setItems(Level.stringValues());
		level.select(level.getItemCount()-1);
		level.setToolTipText("The minimum log level (all higher levels are matched)");
		level.addListener(SWT.Selection, e -> checkIfSearchDirty());

		stack = new Button(filter, SWT.CHECK);
		stack.setText("Only Stacktraces");
		stack.setToolTipText("Match entries with multiple lines only");
		stack.addListener(SWT.Selection, e -> checkIfSearchDirty());

		regex = new Text(filter, SWT.BORDER);
		regex.setMessage("Match messages by regex...");
		regex.setToolTipText("Use a plain search string or a Java-esque regex (use ^$ to match beginning, end or complete message; start with (?i) to match case insensitively)");
		regex.addListener(SWT.KeyUp, e -> checkIfSearchDirty());

		//TODO key bindings
		up = new Button(filter, SWT.PUSH);
		up.setText("\u2191");
		up.setToolTipText("Jump to previous matching entry");
		up.addListener(SWT.Selection, e -> jumpToMatch(Direction.UP));

		down = new Button(filter, SWT.PUSH);
		down.setText("\u2193");
		down.setToolTipText("Jump to next matching entry");
		down.addListener(SWT.Selection, e -> jumpToMatch(Direction.DOWN));

		apply = new Button(filter, SWT.PUSH);
		apply.setText("Filter");
		apply.setToolTipText("Only show matching entries");
		apply.addListener(SWT.Selection, e -> applyFilter());
	}

	private void createStatusView(Composite filter) {
		Button refresh = new Button(filter, SWT.PUSH);
		refresh.setText("Refresh");
		refresh.setToolTipText("Reload config and entries");
		refresh.addListener(SWT.Selection, e -> refresh());

		status = new ProgressBar(filter, SWT.INDETERMINATE);
		status.setLayoutData(new RowData(20,20));

		counter = new Label(filter, SWT.NONE);
		readLines(0);

		working(false);
	}

	private void createTreeView(SashForm sash) {
		viewer = new LogTreeViewer(sash, control);
		this.viewer.addListener(event -> {
			sync.asyncExec(() -> {
				if (event.getType() == Event.STOP) {
					working(false);
				} else if (event.getType() == Event.START) {
					working(true);
				} else if (event.getType() == Event.ENTRY) {
					GuiEntry guiEntry = (GuiEntry) event.getEntry();
					detail.setText(guiEntry.getMessageComplete());
					detail.setStyleRanges(guiEntry.getMessageCompleteStyleRanges(getMatcher()));
				}
			});
		});
	}

	private void createDetailView(SashForm sash) {
		//TODO JFace TextViewer?
		detail = new StyledText(sash, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		detail.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		detail.addListener(SWT.MouseUp, event -> {
			int offset = -1;
			try {
				offset = detail.getOffsetAtLocation(new Point(event.x, event.y));
			} catch (IllegalArgumentException e) {
				return;
			}
			if (offset >= 0) {
				StyleRange range = detail.getStyleRangeAtOffset(offset);
				if (range != null) {
					EntryMessageLink link = (EntryMessageLink) range.data;
					LinkEnvironmentHandler env = new LinkEnvironmentHandler(link);
					env.handle(sash);
				}
			}
		});
	}

	private void checkIfSearchDirty() {
		IEntryMatcher before = control.getLog().getMatcher();
		IEntryMatcher after = getMatcher();
		boolean isDirty = before == null && after.isRestrictive() || before != null && !after.equals(before);
		setSearchButtonsDirty(isDirty);
	}

	private void setSearchButtonsDirty(boolean dirty) {
		Font font = JFaceResources.getDefaultFont();
		if (dirty) {
			if (bold == null) {
				FontDescriptor descriptor = FontDescriptor.createFrom(apply.getFont()).setStyle(SWT.BOLD);
				bold = descriptor.createFont(apply.getDisplay());
			}
			font = bold;
		}
		apply.setFont(font);
		up.setFont(font);
		down.setFont(font);
	}

	private void working(boolean working) {
		if (status != null) status.setVisible(working);
	}

	private void readLines(int lines) {
		counter.setText("(Read ~"+ (lines / 1e6) + "M lines)");
	}

	private void separator(Composite parent) {
		Label separator = new Label(parent, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new RowData(20, 30));
	}

	private IEntryMatcher getMatcher() {
		String regexFilter = regex.getText();
		Level selected = Level.get(level.getSelectionIndex());
		Class<? extends IEntry> type = IEntry.class;
		if (stack.getSelection())
			type = StackedEntry.class;
		IEntryMatcher matcher;
		try {
			matcher = new EntryMatcher(selected, regexFilter, type);
		} catch (PatternSyntaxException e) {
			matcher = new EntryMatcher(selected, "", type);
			MessageDialog.openInformation(null, "Error", "There is an error in your search pattern: \n"+e.getMessage());
		}
		return matcher;
	}

	private void jumpToDate() {
		LogTime ts = new LogTime(date.getYear(), date.getMonth()+1, date.getDay(),
				time.getHours(), time.getMinutes(), time.getSeconds(), 0);
		viewer.jumpToDate(ts);
	}

	private void applyFilter() {
		setSearchButtonsDirty(false);
		viewer.applyFilter(getMatcher());
	}

	private void jumpToMatch(Direction dir) {
		setSearchButtonsDirty(false);
		working(true);
		viewer.jumpToMatch(getMatcher(), dir);
	}

	public void scrollToBottom() {
		viewer.scrollToBottom();
	}

	public void refresh() {
		working(true);
		control.setConfigFile(configFile);
	}

	/**
	 * This method is kept for E3 compatiblity.
	 *
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s == null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of a config file.
	 *
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

		setConfigFileSelection(o);
	}

	/**
	 * This method manages the multiple selection of config files.
	 *
	 * @param o
	 *            : the current array of objects received in case of multiple selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {
		if (selectedObjects != null && selectedObjects.length > 0)
			setConfigFileSelection(selectedObjects[0]);
	}

	private void setConfigFileSelection(Object o) {
		if (o instanceof IFile) {
			IFile config = (IFile) o;
			IPath configFilePath = config.getLocation();
			if (configFilePath != null && configFilePath.getFileExtension().equals(LinkEnvironmentHandler.CONFIG_FILE_EXT)) {
				File newConfigFile = configFilePath.toFile();
				if (!newConfigFile.equals(configFile)) {
					configFile = newConfigFile;
					refresh();
				}
			}
		}
	}
}
