// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.dataanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.User;

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
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		
		boolean interactiveConfig = false;
		
		for (String arg : args) {
			if (arg.equals("--interactiveConfig")) {
				// Configure interactively rather than using embedded cloudcoder.properties
				interactiveConfig = true;
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		Util.configureLogging();
		Properties config = new Properties();
		if (interactiveConfig) {
			Util.readDatabaseProperties(keyboard, config);
		} else {
			Util.loadEmbeddedConfig(config, Retest.class.getClassLoader());
		}
		Util.connectToDatabase(config);
		
		int courseId = Integer.parseInt(Util.ask(keyboard, "Course id: "));
		
		String username = Util.ask(keyboard, "Instructor username: ");
		User user = findUser(username);
		Course course = findCourse(user, courseId);
		
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
		
		// Gather problems
		ProblemList problems = getProblems(user, course); 
		
		// Write assignments file
		writeAssignmentsFile(baseDir, problems);
		
		// Write assignment files
	}

	private static void writeTaggedFile(File baseDir, String path, Map<String, Object> props) throws IOException {
		baseDir.mkdirs();
		File out = new File(baseDir.getPath() + path);
		Writer w = writeToFile(out);
		try {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				String tagname = entry.getKey();
				Object value = entry.getValue();
				
				String line = encodeLine(tagname, value);
				w.write(line);
				w.write("\n");
			}
		} finally {
			IOUtils.closeQuietly(w);
		}
	}

	// Encode a line consisting of a tagname and a value
	private static String encodeLine(String tagname, Object value) throws IOException {
		StringWriter sw = new StringWriter();
		JsonFactory factory = new JsonFactory();
		JsonGenerator jg = factory.createGenerator(sw);
		jg.writeStartObject();
		jg.writeStringField("tag", tagname);
		jg.writeFieldName("value");
		writeJsonFieldValue(jg, value);
		jg.writeEndObject();
		jg.close();
		return sw.toString();
	}

	private static Writer writeToFile(File out) throws FileNotFoundException {
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

	private static User findUser(String username) {
		IDatabase db = Database.getInstance();
		return db.getUserWithoutAuthentication(username);
	}
	
	private static Course findCourse(User user, int courseId) {
		IDatabase db = Database.getInstance();
		List<? extends Object[]> courses = db.getCoursesForUser(user);
		for (Object[] triple : courses) {
			Course course = (Course) triple[0];
			if (course.getId() == courseId) {
				return course;
			}
		}
		throw new IllegalArgumentException("Could not find course " + courseId + " for user " + user.getUsername());
	}

	private static ProblemList getProblems(User user, Course course) {
		IDatabase db = Database.getInstance();
		
		return db.getProblemsInCourse(user, course);
	}

	private static void writeAssignmentsFile(File baseDir, ProblemList problems) throws IOException {
		baseDir.mkdirs();
		Writer w = writeToFile(new File(baseDir, "/assignments.txt"));
		try {
			for (Problem p : problems.getProblemList()) {
				int problemId = p.getProblemId();
				Map<String, Object> obj = new LinkedHashMap<>();
				String path = String.format("/assignment_%04d.txt", problemId);
				obj.put("num", problemId);
				obj.put("path", path);
				String line = encodeLine("assignment", obj);
				w.write(line);
				w.write("\n");
			}
		} finally {
			IOUtils.closeQuietly(w);
		}
	}
}
