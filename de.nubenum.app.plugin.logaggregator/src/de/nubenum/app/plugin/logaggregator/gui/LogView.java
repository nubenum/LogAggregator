package de.nubenum.app.plugin.logaggregator.gui;

import java.io.File;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import de.nubenum.app.plugin.logaggregator.core.UpdateEvent.Event;
import de.nubenum.app.plugin.logaggregator.core.model.Direction;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageLink;
import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.core.model.Level;
import de.nubenum.app.plugin.logaggregator.core.model.LogTime;
import de.nubenum.app.plugin.logaggregator.core.model.entry.IEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.LinedEntry;
import de.nubenum.app.plugin.logaggregator.core.model.entry.StackedEntry;
import de.nubenum.app.plugin.logaggregator.gui.tree.LogTreeViewer;

public class LogView extends EditorPart {

	private LogController control;
	private File configFile;
	private int countLines = 0;
	private int countMBytes = 0;

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

	public LogView() {
		control = new LogController();
		control.addListener(event -> Display.getDefault().asyncExec(() -> {
			if (status.isDisposed())
				return;
			if (event.getType() == Event.COUNT) {
				countLines += event.getNum();
				updateCounter();
			} else if (event.getType() == Event.SIZE) {
				countMBytes += Math.round(event.getNum() / 1024 / 1024);
				updateCounter();
			} else if (event.getType() == Event.EXCEPTION) {
				handleConfigError(event.getException());
			} else if (event.getType() == Event.APPEND) {
				this.viewer.append();
			} else if (event.getType() == Event.REFRESH) {
				this.viewer.refreshOnBottom();
			} else if (event.getType() == Event.STOP) {
				working(false);
			}
		}));
	}

	private void handleConfigError(Exception e) {
		working(false);
		boolean openInEditor = MessageDialog.openConfirm(null, "Error",
				e.getMessage() + "\n\nDo you want to open the config file in a text editor to fix this?");
		if (openInEditor) {
			IFile file = ((FileEditorInput) getEditorInput()).getFile();
			LinkEnvironmentHandler.openTextFile(file);
		}
		updateDetailView(new GuiEntry(new LinedEntry(e.getMessage())));
	}

	@Override
	@Focus
	public void setFocus() {
		if (counter != null)
			counter.setFocus();
	}

	@Override
	@PreDestroy
	public void dispose() {
		if (bold != null)
			bold.dispose();
		control.close();
	}

	@Override
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
		updateCounter();

		working(false);
	}

	private void createTreeView(SashForm sash) {
		viewer = new LogTreeViewer(sash, control);
		this.viewer.addListener(event -> {
			Display.getDefault().asyncExec(() -> {
				if (event.getType() == Event.STOP) {
					control.close(true);
					working(false);
				} else if (event.getType() == Event.START) {
					working(true);
				} else if (event.getType() == Event.ENTRY) {
					GuiEntry guiEntry = (GuiEntry) event.getEntry();
					updateDetailView(guiEntry);
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

	private void updateDetailView(GuiEntry guiEntry) {
		detail.setText(guiEntry.getMessageComplete());
		detail.setStyleRanges(guiEntry.getMessageCompleteStyleRanges(getMatcher(false)));
	}

	private void checkIfSearchDirty() {
		IEntryMatcher before = control.getLog().getMatcher();
		IEntryMatcher after = getMatcher(true);
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

	private void updateCounter() {
		if (counter == null)
			return;
		counter.setText("(~"+ (countLines / 1e6) + "M lines read / ~"+ countMBytes +" MB files opened)");
		counter.pack();
		counter.getParent().pack();
	}

	private void separator(Composite parent) {
		Label separator = new Label(parent, SWT.VERTICAL | SWT.SEPARATOR);
		separator.setLayoutData(new RowData(20, 30));
	}

	private IEntryMatcher getMatcher(boolean silent) {
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
			if (!silent)
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
		viewer.applyFilter(getMatcher(false));
	}

	private void jumpToMatch(Direction dir) {
		setSearchButtonsDirty(false);
		working(true);
		viewer.jumpToMatch(getMatcher(false), dir);
	}

	public void scrollToBottom() {
		viewer.scrollToBottom();
	}

	public void refresh() {
		if (detail != null)
			detail.setText("");
		countLines = 0;
		countMBytes = 0;
		updateCounter();
		working(true);
		//TODO endless repeat after refresh on multi same ts?
		control.setConfigFile(configFile);
	}

	private void setConfigFile(IPath configFilePath) {
		if (configFilePath != null && configFilePath.getFileExtension().equals(LinkEnvironmentHandler.CONFIG_FILE_EXT)) {
			File newConfigFile = configFilePath.toFile();
			if (!newConfigFile.equals(configFile)) {
				configFile = newConfigFile;
				refresh();
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		return;
	}

	@Override
	public void doSaveAs() {
		return;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		IPath path = ((FileEditorInput) input).getPath();
		setConfigFile(path);
		setSite(site);
		setInput(input);

		String name = configFile.getName();
		setPartName(name.substring(0, Math.max(0, name.lastIndexOf("."))));
		setContentDescription(configFile.getParent());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
