package de.nubenum.app.plugin.logaggregator.gui.tree;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import de.nubenum.app.plugin.logaggregator.core.model.IEntryMatcher;
import de.nubenum.app.plugin.logaggregator.gui.GuiEntry;
import de.nubenum.app.plugin.logaggregator.gui.LogController;

public class StyledLogLabelProvider implements IStyledLabelProvider, IColorProvider, IFontProvider {
	private LogController control;

	public StyledLogLabelProvider(LogController control) {
		this.control = control;
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {
		return;
	}

	@Override
	public void dispose() {
		return;
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {
		return;
	}

	@Override
	public Image getImage(Object arg0) {
		return null;
	}

	@Override
	public StyledString getStyledText(Object o) {
		GuiEntry entry = ((GuiEntry) o);
		IEntryMatcher matcher = control.getLog().getMatcher();
		if (matcher != null) {
			return entry.getMessageStyled(matcher);
		}
		return new StyledString(entry.getMessage() != null ? entry.getMessage() : "");
	}

	@Override
	public Font getFont(Object arg0) {
		return JFaceResources.getFont(JFaceResources.TEXT_FONT);
	}

	@Override
	public Color getBackground(Object arg0) {
		return null;
	}

	@Override
	public Color getForeground(Object o) {
		return ((GuiEntry) o).getMessageColor();
	}

}
