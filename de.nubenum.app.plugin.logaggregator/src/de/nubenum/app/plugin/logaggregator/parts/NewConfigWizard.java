package de.nubenum.app.plugin.logaggregator.parts;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class NewConfigWizard extends Wizard implements INewWizard {

	private IStructuredSelection selection;
	private NewConfigWizardPage newConfigWizardPage;
	private IWorkbench workbench;

	public NewConfigWizard() {
		setWindowTitle("New LogAggregator Config File");
	}

	@Override
	public void addPages() {
		newConfigWizardPage = new NewConfigWizardPage(selection);
		addPage(newConfigWizardPage);
	}

	@Override
	public boolean performFinish() {
		IFile file = newConfigWizardPage.createNewFile();
		if (file != null) {
			try {
				IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file);
			} catch (PartInitException e) {
				return true;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}
}
