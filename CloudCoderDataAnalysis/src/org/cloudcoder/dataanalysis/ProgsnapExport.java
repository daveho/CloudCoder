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
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.TestCase;
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
	
	private File baseDir;
	private Properties config;
	private Map<String, Object> datasetProps;
	private int courseId;
	private String username;
	
	public ProgsnapExport() {
		
	}
	
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	public void setConfig(Properties config) {
		this.config = config;
	}
	
	public void setDatasetProps(Map<String, Object> datasetProps) {
		this.datasetProps = datasetProps;
	}
	
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void execute() throws IOException {
		Util.connectToDatabase(config);

		User user = findUser(username);
		Course course = findCourse(user, courseId);
		
		// Ensure that output directory exists
		baseDir.mkdirs();
		
		// Write dataset file
		try (Writer w = writeToFile(new File(baseDir, "/dataset.txt"))) {
			writeTaggedFile(w, datasetProps);
		}
		
		// Gather problems
		ProblemList problems = getProblems(user, course); 
		
		// Write assignments file
		writeAssignmentsFile(problems);
		
		// Write assignment files
		for (Problem p : problems.getProblemList()) {
			writeAssignmentFile(p);
		}
		
		// Write students file
		List<User> users = getUsers(course);
		writeStudentsFile(users, course);
	}

	private void writeTaggedFile(Writer w, Map<String, Object> props) throws IOException {
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			String tagname = entry.getKey();
			Object value = entry.getValue();
			
			String line = encodeLine(tagname, value);
			w.write(line);
			w.write("\n");
		}
	}

	// Encode a line consisting of a tagname and a value
	private String encodeLine(String tagname, Object value) throws IOException {
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

	private Writer writeToFile(File out) throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(out), Charset.forName("UTF-8")));
	}

	private void writeJsonFieldValue(JsonGenerator jg, Object value) throws IOException {
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
		} else if (value instanceof Boolean) {
			jg.writeBoolean(((Boolean)value).booleanValue());
		} else {
			throw new IllegalArgumentException("Don't know how to encode " + value.getClass().getSimpleName() + " as JSON value");
		}
	}

	private User findUser(String username) {
		IDatabase db = Database.getInstance();
		return db.getUserWithoutAuthentication(username);
	}
	
	private Course findCourse(User user, int courseId) {
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

	private ProblemList getProblems(User user, Course course) {
		IDatabase db = Database.getInstance();
		
		return db.getProblemsInCourse(user, course);
	}

	private void writeAssignmentsFile(ProblemList problems) throws IOException {
		Writer w = writeToFile(new File(baseDir, "/assignments.txt"));
		try {
			for (Problem p : problems.getProblemList()) {
				int problemId = p.getProblemId();
				Map<String, Object> obj = new LinkedHashMap<>();
				String path = String.format("/assignment_%04d.txt", problemId);
				obj.put("number", problemId);
				obj.put("path", path);
				String line = encodeLine("assignment", obj);
				w.write(line);
				w.write("\n");
			}
		} finally {
			IOUtils.closeQuietly(w);
		}
	}

	private void writeAssignmentFile(Problem p) throws IOException {
		IDatabase db = Database.getInstance();
		
		Writer w = writeToFile(new File(baseDir, String.format("/assignment_%04d.txt", p.getProblemId())));
		
		try {
			Map<String, Object> assignmentProps = new LinkedHashMap<>();
			// name
			// language
			// assigned
			// due
			assignmentProps.put("name", p.toNiceString());
			assignmentProps.put("language", p.getProblemType().getLanguage().getName());
			assignmentProps.put("assigned", p.getWhenAssigned());
			assignmentProps.put("due", p.getWhenDue());
			writeTaggedFile(w, assignmentProps);
			
			List<TestCase> tests = db.getTestCasesForProblem(p.getProblemId());
			int count = 0;
			for (TestCase t : tests) {
				Map<String, Object> test = new LinkedHashMap<>();
				// number
				// name
				// input
				// output
				// opaque
				// invisible
				test.put("number", count++);
				test.put("name", t.getTestCaseName());
				test.put("input", t.getInput());
				test.put("output", t.getOutput());
				test.put("opaque", t.isSecret());
				test.put("invisible", false);
				String line = encodeLine("test", test);
				w.write(line);
				w.write("\n");
			}
		} finally {
			IOUtils.closeQuietly(w);
		}
	}

	private List<User> getUsers(Course course) {
		IDatabase db = Database.getInstance();
		
		return db.getUsersInCourse(course.getId(), 0);
	}

	private void writeStudentsFile(List<User> users, Course course) throws IOException {
		IDatabase db = Database.getInstance();
		
		try (Writer w = writeToFile(new File(baseDir, "/students.txt"))) {
			for (User user : users) {
				CourseRegistrationList regList = db.findCourseRegistrations(user, course);
				// number
				// instructor
				// FIXME: allow loading of demographic information
				Map<String, Object> student = new LinkedHashMap<>();
				student.put("number", user.getId());
				student.put("instructor", regList.isInstructor());
				String line = encodeLine("student", student);
				w.write(line);
				w.write("\n");
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		ProgsnapExport exporter = new ProgsnapExport();
		
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
		exporter.setConfig(config);
		
		int courseId = Integer.parseInt(Util.ask(keyboard, "Course id: "));
		exporter.setCourseId(courseId);
		
		String username = Util.ask(keyboard, "Instructor username: ");
		exporter.setUsername(username);
		
		File baseDir = new File(Util.ask(keyboard, "Output directory: "));
		exporter.setBaseDir(baseDir);
		
		System.out.println("Enter data set properties:");
		Map<String, Object> datasetProps = new LinkedHashMap<>();
		datasetProps.put("psversion", PSVERSION);
		datasetProps.put("name", Util.ask(keyboard, "Data set name: "));
		datasetProps.put("contact", Util.ask(keyboard, "Contact name: "));
		datasetProps.put("email", Util.ask(keyboard, "Contact email: "));
		datasetProps.put("courseurl", Util.ask(keyboard, "Course URL: "));
		exporter.setDatasetProps(datasetProps);
		
		exporter.execute();
	}
}
