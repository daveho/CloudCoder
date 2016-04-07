// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
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
	private interface DataWriter extends Closeable {
		public Writer writeTo(String fileName) throws IOException;
	}
	
	private static class DirectoryDataWriter implements DataWriter {
		private File baseDir;
		
		public DirectoryDataWriter(File baseDir) {
			this.baseDir = baseDir;
		}

		@Override
		public Writer writeTo(String fileName) throws IOException {
			File f = new File(baseDir, fileName);
			
			// make sure parent directory exists
			File parent = f.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			
			return new OutputStreamWriter(new FileOutputStream(f), Charset.forName("UTF-8"));
		}
		
		@Override
		public void close() throws IOException {
			// Nothing to do
		}
	}
	
	private static class ZipfileDataWriter implements DataWriter {
		private ZipOutputStream zos;
		
		public ZipfileDataWriter(File zipFile) throws FileNotFoundException {
			this.zos = new ZipOutputStream(new FileOutputStream(zipFile));
		}

		@Override
		public Writer writeTo(String fileName) throws IOException {
			ZipEntry entry = new ZipEntry(fileName);
			zos.putNextEntry(entry);
			
			// Return an OutputStreamWriter with the close() method overridden to flush
			// and close the current zip entry.
			return new OutputStreamWriter(zos, Charset.forName("UTF-8")) {
				@Override
				public void close() throws IOException {
					super.flush();
					zos.closeEntry();
				}
			};
		}

		@Override
		public void close() throws IOException {
			zos.close();
		}
	}
	
	private static class WorkHistoryEvent implements Comparable<WorkHistoryEvent> {
		final long ts;
		final String tag;
		final Map<String, Object> value;
		
		WorkHistoryEvent(long ts, String tag, Map<String, Object> value) {
			this.ts = ts;
			this.tag = tag;
			this.value = value;
		}
		
		@Override
		public int compareTo(WorkHistoryEvent o) {
			int cmp;
			
			// Special case: if both objects have edit ids,
			// order them by edit id regardless of how the timestamps compare.
			// I have observed at least one case in CloudCoder of
			// timestamps going backwards in an edit sequence.
			// FIXME: really, in this case, we should fix the timestamps
			if (this.value.containsKey("editid") && o.value.containsKey("editid")) {
				return ((Integer)this.value.get("editid")).compareTo((Integer)o.value.get("editid"));
			}
			
			cmp = ((Long)this.ts).compareTo((Long)o.ts);
			if (cmp != 0) {
				return cmp;
			}

			return this.tag.compareTo(o.tag);
		}
	}
	
	private final class RecordEditEvents implements ICallback<Change> {
		private final Problem problem;
		private final List<WorkHistoryEvent> eventList;

		private RecordEditEvents(Problem problem, List<WorkHistoryEvent> eventList) {
			this.problem = problem;
			this.eventList = eventList;
		}

		public void call(Change value) {
			// ts
			// editid
			// filename
			// type
			// start
			// end
			// text
			int editId = value.getEventId();
			long ts = value.getEvent().getTimestamp();
			String filename = "code" + problem.getProblemType().getLanguage().getFileExtension();
			String type;
			switch (value.getType()) {
			case INSERT_TEXT:
			case INSERT_LINES:
				type = "insert";
				break;
			case REMOVE_TEXT:
			case REMOVE_LINES:
				type = "delete";
				break;
			case FULL_TEXT:
				type = "fulltext";
				break;
			default:
				throw new IllegalStateException("Unknown change type: " + value.getType());
			}
			String text = value.getText();
			if (value.getType() == ChangeType.INSERT_LINES || value.getType() == ChangeType.REMOVE_LINES) {
				// These types of edits occur when ACE notices that entire lines
				// are being inserted or removed.  However, the text of these edits
				// (in ACE, and in the CloudCoder database) don't actually
				// match the precise text being removed, because the
				// final "\n" is missing.  Add it.
				if (!text.endsWith("\n")) {
					text = text + "\n";
				}
			}
			LinkedHashMap<String, Object> start = new LinkedHashMap<>();
			start.put("row", value.getStartRow());
			start.put("col", value.getStartColumn());
			LinkedHashMap<String, Object> end = new LinkedHashMap<>();
			end.put("row", value.getEndRow());
			end.put("col", value.getEndColumn());
			
			LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
			obj.put("ts", ts);
			obj.put("editid", editId);
			obj.put("filename", filename);
			obj.put("type", type);
			obj.put("start", start);
			obj.put("end", end);
			obj.put("text", text);
			
			eventList.add(new WorkHistoryEvent(ts, "edit", obj));
		}
	}

	// Version of progsnap spec the exported data will conform to
	private static final String PSVERSION = "0.1-dev";
	
	private Properties config;
	private DataWriter dataWriter;
	
	public ProgsnapExport() {
		
	}
	
	public void setConfig(Properties config) {
		this.config = config;
	}
	
	public void setDataWriter(DataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}
	
	private String getUsername() {
		return config.getProperty("username");
	}
	
	private int getCourseId() {
		return Integer.valueOf(config.getProperty("courseId"));
	}
	
	private static final Set<String> DATASET_PROPERTY_KEYS = new HashSet<>(
			Arrays.asList("name", "contact", "email", "courseurl"));
	
	private Properties getDatasetProps() {
		Properties datasetProps = new Properties();
		datasetProps.put("psversion", PSVERSION);
		for (Object keyObj : config.keySet()) {
			String key = keyObj.toString();
			if (DATASET_PROPERTY_KEYS.contains(key)) {
				datasetProps.put(key, config.getProperty(key));
			}
		}
		return datasetProps;
	}
	
	public interface IntegerSet {
		public boolean contains(int val);
	}
	
	private static class UniversalIntegerSet implements IntegerSet {
		@Override
		public boolean contains(int val) {
			return true;
		}
	}

	private static class ProblemIdSet implements IntegerSet {
		private final Set<Integer> result;

		private ProblemIdSet(Set<Integer> result) {
			this.result = result;
		}

		@Override
		public boolean contains(int val) {
			return result.contains(val);
		}
	}
	
	private IntegerSet getProblemIds() {
		String problemIds = config.getProperty("problemIds", "").trim();
		if (problemIds.equals("")) {
			return new UniversalIntegerSet();
		}
		final Set<Integer> result = new HashSet<>();
		for (String s : problemIds.split(",")) {
			result.add(Integer.valueOf(s));
		}
		return new ProblemIdSet(result);
	}
	
	public void execute() throws IOException {
		Util.connectToDatabase(config);

		User instructor = findUser(getUsername());
		Course course = findCourse(instructor, getCourseId());
		
		// Write dataset file
		Properties datasetProps = getDatasetProps();
		try (Writer w = dataWriter.writeTo("dataset.txt")) {
			writeTaggedFile(w, datasetProps);
		}
		
		// Gather problems
		ProblemList problems = getProblems(instructor, course); 
		
		// Write assignments file
		writeAssignmentsFile(problems);
		
		// See which problems are being exported
		IntegerSet problemIds = getProblemIds();
		
		// Write assignment files
		for (Problem p : problems.getProblemList()) {
			if (problemIds.contains(p.getProblemId())) {
				writeAssignmentFile(p);
			}
		}
		
		// Write students file
		List<User> users = getUsers(course);
		writeStudentsFile(users, course);
		
		// For each assignment (problem), write student work history files
		for (Problem p : problems.getProblemList()) {
			if (problemIds.contains(p.getProblemId())) {
				for (User student : users) {
					writeStudentWorkHistory(instructor, student, p);
				}
			}
		}
	}

	private void writeTaggedFile(Writer w, Properties props) throws IOException {
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String tagname = entry.getKey().toString();
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
		} else if (value instanceof Object[]) {
			// Encode array as JSON array
			Object[] arr = (Object[]) value;
			jg.writeStartArray();
			for (Object elt : arr) {
				writeJsonFieldValue(jg, elt);
			}
			jg.writeEndArray();
		} else if (value instanceof List) {
			// Encode list as JSON array
			List<?> list = (List<?>) value;
			jg.writeStartArray();
			for (Object elt : list) {
				writeJsonFieldValue(jg, elt);
			}
			jg.writeEndArray();
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
		IntegerSet problemIds = getProblemIds();
		
		try (Writer w = dataWriter.writeTo("assignments.txt")) {
			for (Problem p : problems.getProblemList()) {
				int problemId = p.getProblemId();
				if (!problemIds.contains(problemId)) {
					continue;
				}
				Map<String, Object> obj = new LinkedHashMap<>();
				String path = String.format("assignment/%04d.txt", problemId);
				obj.put("number", problemId);
				obj.put("path", path);
				String line = encodeLine("assignment", obj);
				w.write(line);
				w.write("\n");
			}
		}
	}

	private void writeAssignmentFile(Problem p) throws IOException {
		IDatabase db = Database.getInstance();

		try (Writer w = dataWriter.writeTo(String.format("assignment/%04d.txt", p.getProblemId()))) {
			Properties assignmentProps = new Properties();
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
		}
	}

	private List<User> getUsers(Course course) {
		IDatabase db = Database.getInstance();
		
		List<User> rawUsers = db.getUsersInCourse(course.getId(), 0);
		
		// Ensure that the list doesn't contain duplicates.
		// Duplicates can arise when a single user has multiple course
		// registrations (e.g. an instructor who is teaching multiple
		// sections.)
		Set<User> userSet = new TreeSet<>(new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return ((Integer)o1.getId()).compareTo(o2.getId());
			}
		});
		userSet.addAll(rawUsers);
		
		List<User> result = new ArrayList<>();
		result.addAll(userSet);
		
		return result;
	}

	private void writeStudentsFile(List<User> users, Course course) throws IOException {
		IDatabase db = Database.getInstance();
		
		try (Writer w = dataWriter.writeTo("students.txt")) {
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

	private void writeStudentWorkHistory(final User instructor, final User student, final Problem problem) throws FileNotFoundException, IOException {
		// Build a list of events: it will need to be sorted by timestamp
		List<WorkHistoryEvent> eventList = new ArrayList<>();
		
		// Retrieve edit events
		ICallback<Change> visitor = new RecordEditEvents(problem, eventList);
		Database.getInstance().visitAllChangesNewerThan(student, problem.getProblemId(), -1, visitor, IDatabase.RetrieveChangesMode.RETRIEVE_CHANGES_AND_EDIT_EVENTS);
		
		// Build map of edit ids to edit events
		Map<Integer, WorkHistoryEvent> editEventMap = new HashMap<>();
		for (WorkHistoryEvent evt : eventList) {
			Integer editId = (Integer)evt.value.get("editid");
			//System.out.println("Add edit event " + editId + " to map");
			editEventMap.put(editId, evt);
		}
		
		// Retrieve submission receipts
		SubmissionReceipt[] receipts = Database.getInstance().getAllSubmissionReceiptsForUser(problem, student);
		
		// Use submission receipts to
		// - annotate edit events with snapshot ids (which are submission event ids)
		// - generate submission and compilation events
		for (SubmissionReceipt receipt : receipts) {
			SubmissionStatus status = receipt.getStatus();
			
			if (status == SubmissionStatus.NOT_STARTED || status == SubmissionStatus.STARTED) {
				// Not a real submission
				continue;
			}
			
			// Annotate edit event with snapshot id
			int lastEditId = receipt.getLastEditEventId();
			WorkHistoryEvent editEvent = editEventMap.get(lastEditId);
			if (editEvent == null) {
				throw new IllegalStateException("No such edit id: " + lastEditId);
			}
			@SuppressWarnings("unchecked")
			List<Integer> snapids = (List<Integer>) editEvent.value.get("snapids");
			if (snapids == null) {
				snapids = new ArrayList<>();
				editEvent.value.put("snapids", snapids);
			}
			snapids.add(receipt.getEventId()); // snapshot ids are submission event ids
			
			// Collect submission info, add Submission event
			LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
			long ts = receipt.getEvent().getTimestamp();
			obj.put("ts", ts);
			obj.put("snapid", receipt.getEventId()); // snapshot ids are submission event ids
			eventList.add(new WorkHistoryEvent(ts, "submission", obj));
			
			// Collection compilation info, add Compilation event
			String compilationResult;
			switch (status) {
			case BUILD_ERROR:
			case COMPILE_ERROR:
				compilationResult = "failure";
				break;
			case TESTS_FAILED:
			case TESTS_PASSED:
				compilationResult = "success";
				break;
			default:
				throw new IllegalStateException("Cannot infer compilation result from status " + status);
			}
			LinkedHashMap<String, Object> cObj = new LinkedHashMap<>();
			cObj.put("ts", ts);
			cObj.put("snapid", receipt.getEventId()); // snapshot ids are submission event ids
			cObj.put("result", compilationResult);
			eventList.add(new WorkHistoryEvent(ts, "compilation", cObj));
			
			// Only report test results if testing was actually performed
			if (status == SubmissionStatus.TESTS_PASSED || status == SubmissionStatus.TESTS_FAILED) {
				// Collect test results, add TestResults event.
				// Note that we need to specify the instructor account here to ensure
				// that we can get the test results for any user.
				NamedTestResult[] testResults =
						Database.getInstance().getTestResultsForSubmission(instructor, problem, receipt);
				Object[] statuses = new Object[testResults.length];
				for (int i = 0; i < testResults.length; i++) {
					NamedTestResult tr = testResults[i];
					String trStatus;
					TestOutcome outcome = tr.getTestResult().getOutcome();
					switch (outcome) {
					case FAILED_ASSERTION:
						trStatus = "failed";
						break;
					case FAILED_BY_SECURITY_MANAGER:
					case FAILED_WITH_EXCEPTION:
					case INTERNAL_ERROR:
						trStatus = "exception";
						break;
					case FAILED_FROM_TIMEOUT:
						trStatus = "timeout";
						break;
					case PASSED:
						trStatus = "passed";
						break;
					default:
						throw new IllegalStateException("Can't infer test result status from test outcome " + outcome);
					}
					statuses[i] = trStatus;
				}
				LinkedHashMap<String, Object> trObj = new LinkedHashMap<>();
				trObj.put("ts", ts);
				trObj.put("snapid", receipt.getEventId()); // snapshot ids are submission event ids
				trObj.put("numtests", receipt.getNumTestsAttempted());
				trObj.put("numpassed", receipt.getNumTestsPassed());
				trObj.put("statuses", statuses);
				eventList.add(new WorkHistoryEvent(ts, "testresults", trObj));
			}
		}
		
		// If there were no events for this student/assignment combo,
		// don't bother writing a file.
		if (eventList.isEmpty()) {
			return;
		}
		
		// Sort all work history events by timestamp
		Collections.sort(eventList);
		
		// Write all work history events to the work history file
		
		// Directory name has form "/history/NNNN", where NNNN is the assignment number
		// (i.e., problem number.) Filename is based on student id.
		String fname = String.format("history/%04d/%04d.txt", problem.getProblemId(), student.getId());
		try (Writer w = dataWriter.writeTo(fname)) {
			for (WorkHistoryEvent ev : eventList) {
				w.write(encodeLine(ev.tag, ev.value));
				w.write("\n");
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		ProgsnapExport exporter = new ProgsnapExport();
		
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		
		boolean interactiveConfig = false;
		String specFile = null;
		Properties config = new Properties();
		
		for (String arg : args) {
			if (arg.equals("--interactiveConfig")) {
				// Configure interactively rather than using embedded cloudcoder.properties
				interactiveConfig = true;
			} else if (arg.startsWith("--spec=")) {
				// A "spec" file is properties defining what data should be pulled,
				// what the dataset metadata are, etc.  It can also override
				// cloudcoder.properties configuration values, if desirned (e.g.,
				// database access credentials.)  The idea is to allow repeatable
				// non-interactive exports.
				specFile = arg.substring("--spec=".length());
			} else if (arg.startsWith("-D")) {
				// Set an individual config property
				String keyVal = arg.substring("-D".length());
				int eq = keyVal.indexOf('=');
				if (eq < 0) {
					throw new IllegalArgumentException("Invalid key/value pair: " + keyVal);
				}
				config.setProperty(keyVal.substring(0, eq), keyVal.substring(eq+1));
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		Util.configureLogging();
		if (interactiveConfig) {
			Util.readDatabaseProperties(keyboard, config);
		} else {
			try {
				Util.loadEmbeddedConfig(config, ProgsnapExport.class.getClassLoader());
			} catch (IllegalStateException e) {
				// Attempt to load from cloudcoder.properties in parent directory
				Util.loadFileConfig(config, new File("../cloudcoder.properties"));
				System.out.println("Read cloudcoder.properties in parent directory");
			}
		}
		
		// If a specfile was specified, layer its properties on top of
		// whatever config properties we found.
		if (specFile != null) {
			Properties spec = new Properties();
			try (FileReader fr = new FileReader(specFile)) {
				spec.load(fr);
			}
			
			Properties effectiveSpec = new Properties();
			effectiveSpec.putAll(config);
			effectiveSpec.putAll(spec);
			
			config = effectiveSpec;
		}
		
		askIfMissing(config, "courseId", "Course id: ", keyboard);
		askIfMissing(config, "problemIds", "Problem ids (comma separated, leave blank for all): ", keyboard);
		askIfMissing(config, "username", "Instructor username: ", keyboard);
		askIfMissing(config, "dest", "Output directory or zipfile name: ", keyboard);
		askIfMissing(config, "name", "Data set name: ", keyboard);
		askIfMissing(config, "contact", "Contact name: ", keyboard);
		askIfMissing(config, "email", "Contact email: ", keyboard);
		askIfMissing(config, "courseurl", "Course URL: ", keyboard);
			
		exporter.setConfig(config);

		// Depending on output path, write to a zipfile or a directory.
		String dest = config.getProperty("dest");
		DataWriter dataWriter;
		File destFile = new File(dest);
		dataWriter = dest.toLowerCase().endsWith(".zip")
				? new ZipfileDataWriter(destFile)
				: new DirectoryDataWriter(destFile);
		exporter.setDataWriter(dataWriter);

		// Do the export
		try {
			exporter.execute();
		} finally {
			IOUtils.closeQuietly(dataWriter);
		}
	}

	private static void askIfMissing(Properties config, String propName, String prompt, Scanner keyboard) {
		if (!config.containsKey(propName)) {
			config.setProperty(propName, Util.ask(keyboard, prompt));
		}
	}
}
