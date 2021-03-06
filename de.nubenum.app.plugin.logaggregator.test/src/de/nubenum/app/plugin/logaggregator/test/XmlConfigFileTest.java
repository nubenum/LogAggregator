package de.nubenum.app.plugin.logaggregator.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.nubenum.app.plugin.logaggregator.core.config.IConfig;
import de.nubenum.app.plugin.logaggregator.core.config.XmlConfig;
import de.nubenum.app.plugin.logaggregator.core.config.XmlConfigFile;
import de.nubenum.app.plugin.logaggregator.core.config.XmlLogHost;
import de.nubenum.app.plugin.logaggregator.core.config.XmlLogSource;

public class XmlConfigFileTest {

	@Test
	public void writeAndRead() throws IOException {
		File d = new File("tmp");
		d.mkdir();
		
		XmlConfigFile xml = new XmlConfigFile();
		IConfig conf = new XmlConfig();
		
		List<XmlLogSource> files = new ArrayList<>();
		XmlLogSource f = new XmlLogSource();
		f.setName("file");
		f.setIgnoreNotFound(true);
		files.add(f);
		
		XmlLogSource f2 = new XmlLogSource();
		f2.setName("file2");
		files.add(f2);
		
		conf.setSources(files);
		
		List<XmlLogHost> hosts = new ArrayList<>();
		XmlLogHost h = new XmlLogHost();
		h.setName("host");
		h.setShortName("short");
		hosts.add(h);
		conf.setHosts(hosts);
		
		conf.setLocation("abc");
		
		xml.setConfig(conf);
		xml.setFile(new File("tmp/conf.xml"));
		xml.write();
		
		xml.read();
		IConfig newC = xml.getConfig();
		
		assertEquals(newC.getSources().get(0).getName(), f.getName());
		assertEquals(newC.getSources().get(0).getIgnoreNotFound(), f.getIgnoreNotFound());
		assertEquals(newC.getSources().get(1).getName(), f2.getName());
		assertEquals(newC.getHosts().get(0).getName(), h.getName());
		assertEquals(newC.getHosts().get(0).getShortName(), h.getShortName());
		assertEquals(newC.getLocation(), conf.getLocation());		
	}

}
