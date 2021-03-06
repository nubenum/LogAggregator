package de.nubenum.app.plugin.logaggregator.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import de.nubenum.app.plugin.logaggregator.core.SystemLog;
import de.nubenum.app.plugin.logaggregator.core.config.AutoConfigCreator;
import de.nubenum.app.plugin.logaggregator.core.config.IConfig;
import de.nubenum.app.plugin.logaggregator.core.config.XmlConfig;

public class NewConfigWizardPage extends WizardNewFileCreationPage {
	private String analyzePath;
	private Text pattern;
	private Display display;

	public NewConfigWizardPage(IStructuredSelection selection) {
		super("NewConfigWizardPage", selection);
		setTitle("LogAggregator Config File");
		setDescription("Creates a new Config File for LogAggregator.");
		setFileExtension("logagg");
	}

	@Override
	protected void createAdvancedControls(Composite parent) {
		super.createAdvancedControls(parent);
		display = parent.getDisplay();

		Label label = new Label(parent, SWT.WRAP);
		label.setText("Choose a location that contains a tree of log files to try and generate a suitable config file from that automatically. "
				+ "You will have to adapt the config file to suit your needs with a text or XML editor. "
				+ "If no location is selected, an example file will be generated.");
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 400;
		label.setLayoutData(data);

		Composite chooser = new Composite(parent, SWT.NULL);
		GridLayout hor = new GridLayout();
		hor.numColumns = 2;
		chooser.setLayout(hor);
		chooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button choose = new Button(chooser, SWT.PUSH);
		choose.setText("Log files location...");

		Text path = new Text(chooser, SWT.BORDER);
		path.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		path.setEnabled(false);

		Label patternLabel = new Label(chooser, SWT.WRAP);
		patternLabel.setText("Files to match (Java regex):");

		pattern = new Text(chooser, SWT.BORDER);
		pattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pattern.setText(".*\\.log");

		choose.addListener(SWT.Selection, e -> {
			DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
			dialog.setFilterPath(analyzePath);
			dialog.setMessage("Choose a location containing log files in a tree structure");
			analyzePath = dialog.open();
			path.setText(analyzePath != null ? analyzePath : "");
		});
	}

	@Override
	protected InputStream getInitialContents() {
		if (analyzePath == null) {
			return new ByteArrayInputStream(DefaultConstants.DEFAULT_CONFIG.getBytes());
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			BusyIndicator.showWhile(display, () -> {
				AutoConfigCreator creator = new AutoConfigCreator(Paths.get(analyzePath), Pattern.compile(pattern.getText()));
				IConfig config = creator.create();
				try {
					JAXBContext context = JAXBContext.newInstance(XmlConfig.class);
					Marshaller m = context.createMarshaller();
					m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
					m.marshal(config, out);
				} catch (JAXBException e) {
					SystemLog.log(e);
				}
			});

			return new ByteArrayInputStream(out.toByteArray());
		}
	}
}
