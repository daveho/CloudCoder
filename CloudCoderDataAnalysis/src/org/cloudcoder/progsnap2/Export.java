// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2019, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.progsnap2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.shared.model.ApplyChangeToTextDocument;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.TextDocument;
import org.cloudcoder.app.shared.model.Triple;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.dataanalysis.Util;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVWriter;

public class Export {
	private Properties config;
	private EventFactory eventFactory;
	private long nextEventOrderValue;
	private MainTableWriter mainTableWriter;
	
	private static Export theInstance;
	
	public static Export getExport() {
		if (theInstance == null) {
			throw new IllegalStateException("No singleton Export object");
		}
		return theInstance;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}
	
	public void createEventFactory() {
		//
		// The EventFactory relies on the exporter creating ProgSnap2Event object
		// in the "correct" order, i.e., our best-guess chronological order.
		//
		eventFactory = new EventFactory() {
			@Override
			public ProgSnap2Event createEvent(EventType eventType, long eventId, int subjectId, String termId, String[] toolInstances) {
				ProgSnap2Event evt = ProgSnap2Event.create(eventType, eventId, subjectId, toolInstances);
				evt.setOrder(nextEventOrderValue);
				nextEventOrderValue++;
				evt.setTermId(termId);
				return evt;
			}
		};
	}

	public void setMainTableWriter(MainTableWriter mainTableWriter) {
		this.mainTableWriter = mainTableWriter;
	}

	public void execute() throws IOException {
		Util.connectToDatabase(config);

		User instructor = findUser(getUsername());
		Course course = findCourse(instructor, getCourseId());
		List<User> students = findUsers(course);
		ProblemList problems = findProblems(instructor, course);
		
		IDatabase db = Database.getInstance();
		
		// Write main event table
		for (User student : students) {
			for (Problem problem : problems.getProblemList()) {
				System.out.printf("Retrieving events for student %d, problem %d\n", student.getId(), problem.getProblemId());
				List<Triple<Event, Change, SubmissionReceipt>> events =
						db.retrieveEvents(problem, student);
				System.out.printf("  Retrieved %d events\n", events.size());
				writeEvents(instructor, course, problem, student, events);
			}
		}
		
		// Write README.txt
		FileUtils.copyFile(new File(this.getReadmePath()), new File(mainTableWriter.getBaseDir(), "README.txt"));
		
		// Write DatasetMetadata.csv
		try (FileWriter fw = new FileWriter(new File(mainTableWriter.getBaseDir(), "DatasetMetadata.csv"))) {
			CSVWriter dm = CSV.charset("UTF-8").create().writer(fw);
			dm.writeNext("Property", "Value");
			dm.writeNext("Version", "6");
			dm.writeNext("IsEventOrderingConsistent", "true");
			dm.writeNext("EventOrderScope", "Restricted");
			dm.writeNext("EventOrderScopeColumns", "CourseID;ProblemID;SubjectID");
			dm.writeNext("CodeStateRepresentation", "Directory");
		}
	}

	private void writeEvents(User instructor, Course course, Problem problem, User student,
			List<Triple<Event, Change, SubmissionReceipt>> events) {

		File codeStates = mainTableWriter.makeSubdir("CodeStates");

		TextDocument doc = new TextDocument();
		ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();
		
		String currentCodeStateId = "";
		
		Term term = new Term();
		term.setId(course.getTermId());
		Database.getInstance().reloadModelObject(term);
		String termId = term.getName() + " " + course.getYear();
		
		for (Triple<Event, Change, SubmissionReceipt> triple : events) {
			// Generate ProgSnap2 EventId based on CloudCoder event id, but spaced
			// out to allow for child events (e.g., in the case of a CloudCoder
			// submission receipt, which will generate Compile and Run.Test
			// ProgSnap2 events.)
			long eventId = (long)triple.getFirst().getId() * 40L;
			
			if (triple.getSecond() != null) {
				// This is a Change (i.e., an Edit event)
				Change c = triple.getSecond();

				boolean lastEditTextGood = true;
				if (c.getType() == ChangeType.FULL_TEXT) {
					// If a delta failed to apply, a full text change will allow us to resync
					lastEditTextGood = true;
				}

				if (lastEditTextGood) {
					try {
						// Write the code state
						applicator.apply(c, doc);
						
						// Create the edit event
						ProgSnap2Event evt = eventFactory.createEvent(EventType.FileEdit, eventId, student.getId(), termId, TOOL_INSTANCES);
						//evt.setAssignmentId(0); // CloudCoder doesn't really have the concept of assignments
						evt.setCourseId(problem.getCourseId());
						// TODO: course section id
						evt.setEventInitiator(EventInitiator.User);
						evt.setProblemId(problem.getProblemId());
						evt.setServerTimestamp(c.getEvent().getTimestamp());
						
						//evt.setCodeStateId("c" + c.getEventId());
						
						// To avoid having a huge number of immediate subdirectories in the CodeStates
						// directory, generate CodeStateID values as a hierarchy, user id then
						// edit event id.
						currentCodeStateId = "u" + student.getId() + "/p" + problem.getProblemId() + "/c" + c.getEventId();
						evt.setCodeStateId(currentCodeStateId);
						
						String codeStateSection;
						String sourceFileName = getSourceFileName(problem);
						codeStateSection = getCodeStateSection(currentCodeStateId, sourceFileName);
						evt.setCodeStateSection(codeStateSection);

						// TODO: term id

						// Write the event to the main table
						mainTableWriter.writeEvent(evt);

						File codeStateDir = new File(codeStates, evt.getCodeStateId());
						if (!codeStateDir.mkdirs()) {
							throw new RuntimeException("Could not create code state directory " + codeStateDir);
						}

						File codeFile = new File(codeStateDir, sourceFileName);
						try (FileWriter fw = new FileWriter(codeFile)) {
							fw.write(doc.getText());
						}
					} catch (Exception e) {
						// delta failed to apply, blargh
						lastEditTextGood = false;
					}
				}
			}
			if (triple.getThird() != null) {
				// Submission event
				writeSubmission(eventId, student, problem, termId, triple.getThird(), currentCodeStateId);
			}
		}
	}

	private String getCodeStateSection(String currentCodeStateId, String sourceFileName) {
		String codeStateSection;
		codeStateSection = currentCodeStateId + "/" + sourceFileName;
		return codeStateSection;
	}

	private String getSourceFileName(Problem problem) {
		String sourceFileName = "code" + problem.getProblemType().getLanguage().getFileExtension();
		return sourceFileName;
	}

	/*
	private void writeSessionEvents(Course course) {
		IDatabase db = Database.getInstance();
		SnapshotSelectionCriteria criteria = new SnapshotSelectionCriteria();
		criteria.setCourseId(course.getId());
		List<WorkSession> sessions = db.findWorkSessions(criteria, getSeparationSeconds());

		for (WorkSession session : sessions) {
			Event sessionStart = new Event(EventType.SessionStart, session.getStartEventId(), 0, session.getUserId(), TOOL_INSTANCES);
			sessionStart.setServerTimestamp(session.getStartTime());
		TextDocument doc = new TextDocument();
		ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();

			sessionStart.setProblemId(session.getProblemId());
			sessionStart.setCourseId(session.getCourseId());
			sessionStart.setSessionId(session.getStartEventId());

			Event sessionEnd = new Event(EventType.SessionEnd, session.getEndEventId(), 0, session.getUserId(), TOOL_INSTANCES);
			sessionEnd.setServerTimestamp(session.getEndTime());
			sessionEnd.setProblemId(session.getProblemId());
			sessionEnd.setCourseId(session.getCourseId());
			sessionEnd.setSessionId(session.getStartEventId());

			mainTableWriter.writeEvent(sessionStart);
			mainTableWriter.writeEvent(sessionEnd);
		}
	}
	*/

//	@Deprecated
//	private void writeCompileAndSubmitEvents(User instructor, User student, Problem problem) {
//		IDatabase db = Database.getInstance();
//		SubmissionReceipt[] receipts = db.getAllSubmissionReceiptsForUser(problem, student);
//
//		// Because all receipts have the same eventId, it is unclear how to assign eventIds
//		// for all derived events (Submit, Compile, Compile.Error, Run.Test) without
//		// assigning duplicates. One option may be to decouple the retrieved from the database
//		// and the eventId assigned to a ProgSnap2 Event. We could write arbitrary values (from a
//		// counter, guid, etc) as long as they maintain referential integrity.
//		for (SubmissionReceipt receipt : receipts) {
//			writeSubmission(student, problem, receipt, "unknown code state");
//		}
//	}

	private void writeSubmission(long submitEventId, User student, Problem problem, String termId, SubmissionReceipt receipt, String currentCodeStateId) {
		IDatabase db = Database.getInstance();

		SubmissionStatus status = receipt.getStatus();

		mainTableWriter.makeSubdir("CodeStates");

		// Not a real submission
		if (status == SubmissionStatus.NOT_STARTED) {
			//continue;
		}

		// Record File.Open if they have only STARTED
		else if (status == SubmissionStatus.STARTED) {
			// DHH: I don't think there's a lot of value in generating File.Open events
			// for CloudCoder data.
			/*
			ProgSnap2Event fileOpen = eventFactory.createEvent(EventType.FileOpen, eventId, student.getId(), TOOL_INSTANCES);
			fileOpen.setServerTimestamp(receipt.getEvent().getTimestamp());
			fileOpen.setProblemId(problem.getProblemId());
			fileOpen.setCourseId(problem.getCourseId());
			// It would be nice to have sessionId
			mainTableWriter.writeEvent(fileOpen, currentCodeStateId);
			//continue;
			 */
		} else {
			ProgSnap2Event submit = null;
			ProgSnap2Event compile = null;
			ProgSnap2Event compileError = null;
			NamedTestResult[] tests = null;
			List<ProgSnap2Event> runTests = new ArrayList<ProgSnap2Event>();
			
			// Determine CodeStateSection
			String codeStateSection = getCodeStateSection(currentCodeStateId, getSourceFileName(problem));

			// Record Submit event
			submit = eventFactory.createEvent(EventType.Submit, submitEventId, student.getId(), termId, TOOL_INSTANCES);
			
			// Create an ExecutionID to link Run.Test events associated
			// with this submission.
			String executionId = "ex" + submit.getEventId();

			submit.setServerTimestamp(receipt.getEvent().getTimestamp());
			submit.setProblemId(problem.getProblemId());
			submit.setCourseId(problem.getCourseId());
			if (status == SubmissionStatus.TESTS_FAILED || status == SubmissionStatus.TESTS_PASSED) {
				// Tests were apparently executed, so record an executionID
				submit.setExecutionId(executionId);
			}
			submit.setCodeStateSection(codeStateSection);
			// It would be nice to have sessionId
			//mainTableWriter.writeEvent(submit, currentCodeStateId);

			ProgramResult programResult = ProgramResult.Success;
			if (status == SubmissionStatus.COMPILE_ERROR || status == SubmissionStatus.BUILD_ERROR) {
				programResult = ProgramResult.Error;
			}

			// Record Compile event
			long compileEventId = submitEventId + 1L;
			compile = eventFactory.createEvent(EventType.Compile, compileEventId, student.getId(), termId, TOOL_INSTANCES);
			compile.setParentEventId(submitEventId);
			compile.setServerTimestamp(receipt.getEvent().getTimestamp());
			compile.setProblemId(problem.getProblemId());
			compile.setCourseId(problem.getCourseId());
			compile.setEventInitiator(EventInitiator.User);
			// It would be nice to have sessionId
			compile.setProgramResult(programResult);
			compile.setCodeStateSection(codeStateSection);
			//mainTableWriter.writeEvent(compile, currentCodeStateId);

			// Record Compile.Error if necessary
			if (programResult == ProgramResult.Error) {
				long compileErrorEventId = compileEventId + 1L;
				compileError = eventFactory.createEvent(EventType.CompileError, compileErrorEventId, student.getId(), termId, TOOL_INSTANCES);
				compileError.setParentEventId(compileEventId);
				compileError.setServerTimestamp(receipt.getEvent().getTimestamp());
				compileError.setProgramResult(programResult);
				compileError.setCodeStateSection(codeStateSection);
				// No compile message ):
			} else {
				// Record Run.Test events
				if (status == SubmissionStatus.TESTS_PASSED || status == SubmissionStatus.TESTS_FAILED) {
					tests = db.getTestResultsForSubmission(student, problem, receipt);

					int testCount = 1;
					for(NamedTestResult test : tests) {
						TestResult t = test.getTestResult();
						
						long runTestEventId = compileEventId + (long)testCount;

						ProgSnap2Event runTestEvent = eventFactory.createEvent(EventType.RunTest, runTestEventId, student.getId(), termId, TOOL_INSTANCES);
						// note that TestID is qualified with problem id, to ensure uniqueness
						runTestEvent.setTestId("p" + problem.getProblemId() + "/" + test.getTestCaseName());
						runTestEvent.setParentEventId(submitEventId);
						runTestEvent.setServerTimestamp(receipt.getEvent().getTimestamp());
						runTestEvent.setProblemId(problem.getProblemId());
						runTestEvent.setCourseId(problem.getCourseId());
						//runTestEvent.setParentEventId(receipt.getEventId());
						runTestEvent.setProgramInput(t.getInput());
						runTestEvent.setProgramOutput(t.getActualOutput());
						runTestEvent.setEventInitiator(EventInitiator.User);
						runTestEvent.setExecutionId(executionId);
						switch (t.getOutcome()){
						case PASSED:
							runTestEvent.setProgramResult(ProgramResult.Success);
							break;
						case FAILED_ASSERTION:
						case FAILED_WITH_EXCEPTION:
						case FAILED_BY_SECURITY_MANAGER:
						case FAILED_FROM_TIMEOUT:
						case INTERNAL_ERROR:
							runTestEvent.setProgramResult(ProgramResult.Error);
						}
						runTestEvent.setExecutionId(executionId);
						
						//mainTableWriter.writeEvent(runTestEvent, currentCodeStateId);
						runTests.add(runTestEvent);
						
						testCount++;
					}
				}
			}
			
			// Add Score values if appropriate
			boolean scoreKnown = 
					(status == SubmissionStatus.COMPILE_ERROR) ||
					(status == SubmissionStatus.TESTS_FAILED) ||
					(status == SubmissionStatus.TESTS_PASSED);
			if (scoreKnown) {
				// Actually figure out the score for the submission and (if there are any)
				// the tests.
				if (tests != null) {
					int numTestsExecuted = 0;
					int numTestsPassed = 0;
					for (int i = 0; i < tests.length; i++) {
						NamedTestResult tr = tests[i];
						if (tr.getTestResult().getOutcome() == TestOutcome.PASSED) {
							// This test definitively passed
							numTestsExecuted++;
							numTestsPassed++;
							runTests.get(i).setScore(1.0);
						} else if (tr.getTestResult().getOutcome() != TestOutcome.INTERNAL_ERROR) {
							// This test definitively failed
							numTestsExecuted++;
							runTests.get(i).setScore(0.0);
						}
					}
					
					if (numTestsExecuted > 0) {
						// At least one test was executed, so we can compute a score
						// for the submission.
						submit.setScore((double)numTestsPassed / (double)numTestsExecuted);
					}
				}
			}
			
			if (status == SubmissionStatus.COMPILE_ERROR) {
				// Submission failed to compile, so its score is 0
				submit.setScore(0.0);
			}
			
			// Now that scores have been computed (if appropriate),
			// we can write all of the events related to this submission
			mainTableWriter.writeEvent(submit, currentCodeStateId);
			mainTableWriter.writeEvent(compile, currentCodeStateId);
			if (compileError != null) {
				mainTableWriter.writeEvent(compileError, currentCodeStateId);
			}
			for (ProgSnap2Event runTest : runTests) {
				mainTableWriter.writeEvent(runTest, currentCodeStateId);
			}
		}
	}

	public String getUsername() {
		return config.getProperty("ps2.username");
	}

	public int getCourseId() {
		return Integer.valueOf(config.getProperty("ps2.courseId"));
	}
	
	public String getServerTimezone() {
		return config.getProperty("ps2.serverTimezone");
	}
	
	public String getReadmePath() {
		return config.getProperty("ps2.readmePath");
	}

//	private int getSeparationSeconds() {
//		return Integer.valueOf(config.getProperty("separationSeconds"));
//	}

	private ProblemList findProblems(User user, Course course) {
		IDatabase db = Database.getInstance();
		return db.getProblemsInCourse(user, course);
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

	private List<User> findUsers(Course course) {
		IDatabase db = Database.getInstance();
		List<User> rawUsers = db.getUsersInCourse(course.getId(), 0);

		// Ensure that the list doesn't contain duplicates.
		// Duplicates can arise when a single user has multiple course
		// registrations (e.g. an instructor who is teaching multiple
		// sections.)
		Set<User> userSet = new TreeSet<>(new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return ((Integer) o1.getId()).compareTo(o2.getId());
			}
		});
		userSet.addAll(rawUsers);

		List<User> result = new ArrayList<>();
		result.addAll(userSet);

		return result;
	}

	public static void main(String[] args) throws IOException {
		Export exporter = new Export();
		theInstance = exporter;

		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);

		Properties config = getExportConfig(args, keyboard);

		exporter.setConfig(config);

		File destDir = new File(config.getProperty("ps2.dest"));
		MainTableWriter mainTableWriter = new MainTableWriter(destDir);
		exporter.setMainTableWriter(mainTableWriter);
		
		exporter.createEventFactory();

		// Do the export
		try {
			exporter.execute();
		} finally {
			IOUtils.closeQuietly(mainTableWriter);
		}
	}

	public static Properties getExportConfig(String[] args, Scanner keyboard)
			throws FileNotFoundException, IOException {
		Properties config;
		config = new Properties();
		boolean interactiveConfig = false;
		String specFile = null;

		for (String arg : args) {
			if (arg.equals("--interactiveConfig")) {
				// Configure interactively rather than using embedded cloudcoder.properties
				interactiveConfig = true;
			} else if (arg.startsWith("--spec=")) {
				// A "spec" file is properties defining what data should be pulled,
				// what the dataset metadata are, etc. It can also override
				// cloudcoder.properties configuration values, if desired (e.g.,
				// database access credentials.) The idea is to allow repeatable
				// non-interactive exports.
				specFile = arg.substring("--spec=".length());
			} else if (arg.startsWith("-D")) {
				// Set an individual config property
				String keyVal = arg.substring("-D".length());
				int eq = keyVal.indexOf('=');
				if (eq < 0) {
					throw new IllegalArgumentException("Invalid key/value pair: " + keyVal);
				}
				config.setProperty(keyVal.substring(0, eq), keyVal.substring(eq + 1));
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		Util.configureLogging();
		if (interactiveConfig) {
			Util.readDatabaseProperties(keyboard, config);
		} else {
			try {
				Util.loadEmbeddedConfig(config, Export.class.getClassLoader());
			} catch (IllegalStateException e) {
				// Attempt to load from cloudcoder.properties in same directory
				Util.loadFileConfig(config, new File("cloudcoder.properties"));
				System.out.println("Read cloudcoder.properties in same directory");
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

		askIfMissing(config, "ps2.courseId", "Course id: ", keyboard);
		askIfMissing(config, "ps2.username", "Instructor username: ", keyboard);
		askIfMissing(config, "ps2.separationSeconds", "Session separation in seconds: ", keyboard);
		askIfMissing(config, "ps2.dest", "Progsnap2 output directory: ", keyboard);
		askIfMissing(config, "ps2.serverTimezone", "Server timezone", keyboard);
		askIfMissing(config, "ps2.readmePath", "Path of README.txt file", keyboard);
		return config;
	}

	public static void askIfMissing(Properties config, String propName, String prompt, Scanner keyboard) {
		if (!config.containsKey(propName)) {
			config.setProperty(propName, Util.ask(keyboard, prompt));
		}
	}

	// TODO: Include version number and language student is using
	private static String[] TOOL_INSTANCES = { "CloudCoder 0.1.4" }; // FIXME: should not hard code version number
}