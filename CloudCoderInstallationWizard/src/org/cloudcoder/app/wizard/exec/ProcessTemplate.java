package org.cloudcoder.app.wizard.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.wizard.exec.Bootstrap.TestCloudInfo;
import org.cloudcoder.app.wizard.model.BooleanValue;
import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;
import org.cloudcoder.app.wizard.model.Page;
import org.cloudcoder.app.wizard.model.StringValue;
import org.cloudcoder.app.wizard.model.validators.NoopValidator;

public class ProcessTemplate {
	private ImmutableStringValue template;
	private Document document;
	
	public ProcessTemplate(ImmutableStringValue template, Document document, ICloudInfo info) {
		this.template = template;
		this.document = document;
		
		if (!document.hasPage("cloudInfo")) {
			// Add values from ICloudInfo.
			// In theory this could be more dynamic (i.e.,
			// treat the info object as a Java Bean.)
			// But that would be much more complicated.
			Page cloudInfoPage = new Page("cloudInfo", "Cloud Info");
			addCloudInfo(cloudInfoPage, "webappPublicIp", info.getWebappPublicIp());
			addCloudInfo(cloudInfoPage, "webappPrivateIp", info.getWebappPrivateIp());
			addCloudInfo(cloudInfoPage, "dataDir", info.getDataDir().getAbsolutePath());
			document.addPage(cloudInfoPage);
		}
	}
	
	private void addCloudInfo(Page page, String name, String value) {
		page.add(new StringValue(name, name, value), NoopValidator.INSTANCE);
	}

	private static final Pattern SUBST = Pattern.compile("\\$\\{[A-Za-z\\.]+\\}");
	
	public String generate() {
		StringWriter w = new StringWriter();
		BufferedReader r = null;
		
		try {
			r = new BufferedReader(new StringReader(template.getString()));
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}
				while (line.length() > 0) {
					Matcher m = SUBST.matcher(line);
					if (!m.find()) {
						// No (more) substitutions on this line
						w.write(line);
						break;
					}
					w.write(line.substring(0, m.start())); // write literal text preceding the match
					String name = line.substring(m.start() + 2, m.end() - 1); // get name
					String value = document.getValue(name).getObject().toString(); // get the value
					w.write(value);
					
					line = line.substring(m.end());
				}
				w.write("\n");
			}
		} catch (IOException e) {
			throw new IllegalStateException("This cannot happen", e);
		} finally {
			IOUtils.closeQuietly(r);
			IOUtils.closeQuietly(w);
		}
		
		return w.toString();
	}
	
	// Just for testing
	public static void main(String[] args) {
		ImmutableStringValue template = ImmutableStringValue.createHelpText("finished", "reporttemplate", "Report template");
		Document document = new Document();
		Page dnsPage = new Page("dns", "dns");
		dnsPage.add(new StringValue("hostname", "hostname", "cloudcoder-test.duckdns.org"), NoopValidator.INSTANCE);
		dnsPage.add(new BooleanValue("useDuckDns", "useDuckDns", true), NoopValidator.INSTANCE);
		document.addPage(dnsPage);
		Page awsKeypairPage = new Page("awsKeypair", "awsKeypair");
		awsKeypairPage.add(new BooleanValue("useExisting", "useExisting", true), NoopValidator.INSTANCE);
		document.addPage(awsKeypairPage);
		
		ICloudInfo info = new Bootstrap.TestCloudInfo("ccuser", "1.2.3.4", "/home/ccuser/my-keypair.pem", "10.0.0.222");
		info.setDataDir(InstallationConstants.DATA_DIR);
		
		ProcessTemplate pt = new ProcessTemplate(template, document, info);
		
		System.out.print(pt.generate());
	}
}
