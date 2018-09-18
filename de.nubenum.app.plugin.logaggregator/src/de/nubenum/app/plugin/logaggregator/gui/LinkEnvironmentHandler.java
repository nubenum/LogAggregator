package de.nubenum.app.plugin.logaggregator.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IDE;

import de.nubenum.app.plugin.logaggregator.core.model.EntryMessageLink;

public class LinkEnvironmentHandler {
	public static final String CONFIG_FILE_EXT = "logagg";

	private EntryMessageLink link;

	public LinkEnvironmentHandler(EntryMessageLink link) {
		this.link = link;
	}

	private IFile getAbsoluteFile(IPath relative) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(relative);
	}

	private IFile saveFileAs(Composite container) {
		SaveAsDialog dialog = new SaveAsDialog(container.getShell());
		dialog.setOriginalName("config." + CONFIG_FILE_EXT);
		dialog.open();
		IPath path = dialog.getResult();
		if (path != null) {
			if (!path.getFileExtension().equals(CONFIG_FILE_EXT))
				path = path.addFileExtension(CONFIG_FILE_EXT);
			return getAbsoluteFile(path);
		}
		return null;
	}

	private void createDefaultConfigFile(Composite container) {
		IFile file = saveFileAs(container);
		if (file != null)
		{
			InputStream source = new ByteArrayInputStream(DefaultConstants.DEFAULT_CONFIG.getBytes());
			try {
				file.create(source, IResource.NONE, null);
			} catch (CoreException e) {
				MessageDialog.openInformation(null, "Error", "Unable to create file: \n"+e.getMessage());
			}
			openFileAtLine(file, 0);
		}
	}

	private boolean executeVirtualAction(Composite container) {
		if (link.getLinkedPackage().equals(DefaultConstants.VIRTUAL_ACTION)) {
			if (link.getLinkedMethod().equals(DefaultConstants.VIRTUAL_ACTION_CREATE_DEFAULT))
				createDefaultConfigFile(container);
			return true;
		}
		return false;
	}

	private void openLink() {
		SearchEngine s = new SearchEngine();
		NullProgressMonitor monitor = new NullProgressMonitor();
		TypeNameMatchRequestor collector = new TypeNameMatchRequestor() {
			private boolean found = false;
			@Override
			public void acceptTypeNameMatch(TypeNameMatch result) {
				if (found) return;
				IPath path = result.getType().getPath();
				openFileAtLine(getAbsoluteFile(path), link.getLinkedLine());
				monitor.setCanceled(true);
				found = true;
			}
		};

		try {
			s.searchAllTypeNames(link.getLinkedPackage().toCharArray(), SearchPattern.R_EXACT_MATCH, link.getLinkedClass().toCharArray(), IJavaSearchConstants.TYPE, IJavaSearchConstants.TYPE, SearchEngine.createWorkspaceScope(), collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
		} catch (OperationCanceledException e) {
			return;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private static void openFileAtLine(IFile file, int line) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttribute(IMarker.LINE_NUMBER, line);
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			return;
		}
	}

	public static void openTextFile(IFile file) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttribute(IDE.EDITOR_ID_ATTR, "org.eclipse.ui.DefaultTextEditor");
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			return;
		}
	}

	public void handle(Composite container) {
		if (!executeVirtualAction(container))
			openLink();
	}
}
