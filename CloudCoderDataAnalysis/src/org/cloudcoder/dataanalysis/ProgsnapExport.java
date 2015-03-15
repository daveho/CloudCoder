package org.cloudcoder.dataanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.Course;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Export data about a CloudCoder {@link Course} in
 * <a href="http://cloudcoderdotorg.github.io/progsnap-spec/">progsnap</a>
 * format.
 * 
 * @author David Hovemeyer
 */
public class ProgsnapExport {
	// Version of progsnap spec the exported data will conform to
	private static final String PSVERSION = "0.0-dev";
	
	public static void main(String[] args) throws IOException {
		Scanner keyboard = new Scanner(System.in);
		
		File baseDir = new File(Util.ask(keyboard, "Output directory: "));
		
		System.out.println("Enter data set properties:");
		Map<String, Object> datasetProps = new LinkedHashMap<>();
		datasetProps.put("psversion", PSVERSION);
		datasetProps.put("name", Util.ask(keyboard, "Data set name: "));
		datasetProps.put("contact", Util.ask(keyboard, "Contact name: "));
		datasetProps.put("email", Util.ask(keyboard, "Contact email: "));
		datasetProps.put("courseurl", Util.ask(keyboard, "Course URL: "));
		
		// Write dataset file
		writeTaggedFile(baseDir, "/dataset.txt", datasetProps);
	}

	private static void writeTaggedFile(File baseDir, String path, Map<String, Object> props) throws IOException {
		baseDir.mkdirs();
		File out = new File(baseDir.getPath() + path);
		BufferedWriter w = writeToFile(out);
		try {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				StringWriter sw = new StringWriter();
				
				JsonFactory factory = new JsonFactory();
				JsonGenerator jg = factory.createGenerator(sw);
				jg.writeStartObject();
				jg.writeStringField("tag", entry.getKey());
				jg.writeFieldName("value");
				writeJsonFieldValue(jg, entry.getValue());
				jg.writeEndObject();
				jg.close();
				
				w.write(sw.toString());
				w.write("\n");
			}
		} finally {
			IOUtils.closeQuietly(w);
		}
	}

	private static BufferedWriter writeToFile(File out) throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), Charset.forName("UTF-8")));
	}

	private static void writeJsonFieldValue(JsonGenerator jg, Object value) throws IOException {
		if (value instanceof String) {
			jg.writeString((String)value);
		} else if (value instanceof Integer) {
			jg.writeNumber(((Integer)value).intValue());
		} else if (value instanceof Long) {
			jg.writeNumber(((Long)value).longValue());
		} else if (value instanceof Double) {
			jg.writeNumber(((Double)value).doubleValue());
		} else if (value instanceof Map) {
			jg.writeStartObject();
			for (Map.Entry<?,?> entry : ((Map<?, ?>)value).entrySet()) {
				jg.writeFieldName(entry.getKey().toString());
				writeJsonFieldValue(jg, entry.getValue());
			}
			jg.writeEndObject();
		}
	}
}
