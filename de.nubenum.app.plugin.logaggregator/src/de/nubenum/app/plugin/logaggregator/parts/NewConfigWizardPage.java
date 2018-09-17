package de.nubenum.app.plugin.logaggregator.parts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import de.nubenum.app.plugin.logaggregator.config.AutoConfigCreator;
import de.nubenum.app.plugin.logaggregator.config.IConfig;
import de.nubenum.app.plugin.logaggregator.config.XmlConfig;
import de.nubenum.app.plugin.logaggregator.core.SystemLog;

public class NewConfigWizardPage extends WizardNewFileCreationPage {
	public NewConfigWizardPage(IStructuredSelection selection) {
		super("NewConfigWizardPage", selection);
		setTitle("LogAggregator Config File");
		setDescription("Creates a new example Config File for LogAggregator. You will have to adapt the config file to suit your needs with a text or XML editor.");
		setFileExtension("logagg");
	}

	@Override
	protected InputStream getInitialContents() {
		//return new ByteArayInputStream(DefaultConstants.DEFAULT_CONFIG.getBytes());
		AutoConfigCreator creator = new AutoConfigCreator(Paths.get("E:/logs/logs/"));
		IConfig config = creator.create();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			JAXBContext context = JAXBContext.newInstance(XmlConfig.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(config, out);
		} catch (JAXBException e) {
			SystemLog.log(e);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}
}
