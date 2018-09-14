package de.nubenum.app.plugin.logaggregator.parts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewConfigWizardPage extends WizardNewFileCreationPage {

	public NewConfigWizardPage(IStructuredSelection selection) {
		super("NewConfigWizardPage", selection);
		setTitle("LogAggregator Config File");
		setDescription("Creates a new example Config File for LogAggregator. You will have to adapt the config file to suit your needs with a text or XML editor.");
		setFileExtension("logagg");
	}

	@Override
	protected InputStream getInitialContents() {
		return new ByteArrayInputStream(DefaultConstants.DEFAULT_CONFIG.getBytes());
	}
}
