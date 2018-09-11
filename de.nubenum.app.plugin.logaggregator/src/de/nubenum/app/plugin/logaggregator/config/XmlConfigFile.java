package de.nubenum.app.plugin.logaggregator.config;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XmlConfigFile implements IConfigFile {
	private JAXBContext context;
	private File file;
	private IConfig config; 
	
	public XmlConfigFile() throws IOException {
		try {
			context = JAXBContext.newInstance(XmlConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public IConfig getConfig() {
		return config;
	}
	
	public void setConfig(IConfig config) {
		this.config = config;
	}
	
	public void read() throws IOException {
		try {
			Unmarshaller um = context.createUnmarshaller();
			config = (IConfig) um.unmarshal(file);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}
	
	public void write() throws IOException {
		try {
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(config, file);
		} catch (JAXBException e) {
			throw new IOException(e);
		}		
	}
}
