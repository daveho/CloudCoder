package org.cloudcoder.app.wizard.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ImmutableStringValue extends StringValue {

	public ImmutableStringValue(String name, String value) {
		super(name);
		super.setString(value);
	}
	
	@Override
	public ValueType getValueType() {
		return ValueType.IMMUTABLE_STRING;
	}

	@Override
	public void setString(String value) {
		throw new IllegalArgumentException();
	}
	
	public static ImmutableStringValue createHelpText(String pageName, String name) {
		String resName = "org/cloudcoder/app/wizard/res/" + pageName + "/" + name + ".html";
		try {
			ClassLoader cl = ImmutableStringValue.class.getClassLoader();
			InputStream in = cl.getResourceAsStream(resName);
			if (in == null) {
				throw new IllegalArgumentException("Cannot find resource " + pageName + "." + name);
			}
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
				StringBuilder buf = new StringBuilder();
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					buf.append(line);
					buf.append('\n');
				}
				return new ImmutableStringValue(name, buf.toString());
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read resource " + pageName + "." + name);
		}
	}
}
