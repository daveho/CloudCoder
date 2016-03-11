package org.cloudcoder.app.wizard.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.cloudcoder.app.wizard.exec.InstallationConstants;

public class LoadPreviousCcinstallProperties implements IPageNavigationHook {
	@Override
	public void onNext(Document document) {
		File f = new File(InstallationConstants.DATA_DIR, "ccinstall.properties");
		Properties props = new Properties();
		
		try (FileReader fr = new FileReader(f)) {
			props.load(fr);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load properties from " + f.getAbsolutePath());
		}
		
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String propName = entry.getKey().toString();
			if (DocumentFactory.isInternalProperty(propName)) {
				// Internal config variable, don't load
				continue;
			}
			try {
				IValue value = document.getValue(propName);
				value.setPropertyValue(entry.getValue().toString());
			} catch (NoSuchElementException e) {
				System.err.println("ccinstall.properties has unknown property " + propName);
			}
		}
	}
}
