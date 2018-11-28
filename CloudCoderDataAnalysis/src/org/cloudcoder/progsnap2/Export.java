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

package org.cloudcoder.progsnap2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.WorkSession;
import org.cloudcoder.dataanalysis.Util;

public class Export {
    private Properties config;
    private MainTableWriter mainTableWriter;
    private int prevEventId;

    public void setConfig(Properties config) {
        this.config = config;
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

        writeSessionEvents(course);

        for (Problem problem : problems.getProblemList()) {
            for (User student : students) {
                writeCompileAndSubmitEvents(instructor, student, problem);
            }
        }
    }

    private void writeSessionEvents(Course course) {
        IDatabase db = Database.getInstance();
        SnapshotSelectionCriteria criteria = new SnapshotSelectionCriteria();
        criteria.setCourseId(course.getId());
        List<WorkSession> sessions = db.findWorkSessions(criteria, getSeparationSeconds());

        for (WorkSession session : sessions) {
            Event sessionStart = new Event(EventType.SessionStart, session.getStartEventId(), 0, session.getUserId(), TOOL_INSTANCES);
            sessionStart.setServerTimestampt(session.getStartTime());
            sessionStart.setProblemId(session.getProblemId());
            sessionStart.setCourseId(session.getCourseId());
            sessionStart.setSessionId(session.getStartEventId());

            Event sessionEnd = new Event(EventType.SessionEnd, session.getEndEventId(), 0, session.getUserId(), TOOL_INSTANCES);
            sessionEnd.setServerTimestampt(session.getEndTime());
            sessionEnd.setProblemId(session.getProblemId());
            sessionEnd.setCourseId(session.getCourseId());
            sessionEnd.setSessionId(session.getStartEventId());

            mainTableWriter.writeEvent(sessionStart);
            mainTableWriter.writeEvent(sessionEnd);
        }
    }

    // This should really be borrowing more from ProgsnapExport.writeStudentWorkHistory
    private void writeCompileAndSubmitEvents(User instructor, User student, Problem problem) {
        IDatabase db = Database.getInstance();
        SubmissionReceipt[] receipts = db.getAllSubmissionReceiptsForUser(problem, student);

        // TODO: Because all receipts have the same eventId, it is unclear how to assign eventIds
        // for all derived events (Submit, Compile, Compile.Error, etc) without assigning duplicates.
        // One option may be to decouple the eventId retrieved from the database and the eventId
        // assigned to a ProgSnap2 Event. We could write arbitrary values (from a counter, guid, etc)
        // as long as they maintain referential integrity.
        for (SubmissionReceipt receipt : receipts) {
            SubmissionStatus status = receipt.getStatus();

            if (status == SubmissionStatus.STARTED || status == SubmissionStatus.NOT_STARTED) {
                continue;
            }

            ProgramResult programResult = ProgramResult.Success;

            if (status == SubmissionStatus.COMPILE_ERROR || status == SubmissionStatus.BUILD_ERROR) {
                programResult = ProgramResult.Error;
            }

            Event submit = new Event(EventType.Submit, receipt.getEventId(), 0, student.getId(), TOOL_INSTANCES);
            submit.setServerTimestampt(receipt.getEvent().getTimestamp());
            submit.setProblemId(problem.getProblemId());
            submit.setCourseId(problem.getCourseId());
            // It would be nice to have sessionId
            mainTableWriter.writeEvent(submit);

            Event compile = new Event(EventType.Compile, receipt.getEventId(), 0, student.getId(), TOOL_INSTANCES);
            compile.setServerTimestampt(receipt.getEvent().getTimestamp());
            compile.setProblemId(problem.getProblemId());
            compile.setCourseId(problem.getCourseId());
            // It would be nice to have sessionId
            compile.setProgramResult(programResult);
            mainTableWriter.writeEvent(compile);

            if (programResult == ProgramResult.Error) {
                Event compileError = new Event(EventType.CompileError, receipt.getEventId(), 0, student.getId(), TOOL_INSTANCES);
                compileError.setServerTimestampt(receipt.getEvent().getTimestamp());
                compileError.setProgramResult(programResult);
                // No compile message ):
                // It would be nice to have sessionId
                // ParentEventID
                mainTableWriter.writeEvent(compileError);
            }
        }
    }

    private int peakNextEventId() {
        return prevEventId + 1;
    }

    private int getNextEventId() {
        return ++prevEventId;
    }

    private String getUsername() {
        return config.getProperty("username");
    }

    private int getCourseId() {
        return Integer.valueOf(config.getProperty("courseId"));
    }

    private int getSeparationSeconds() {
        return Integer.valueOf(config.getProperty("separationSeconds"));
    }

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
                // what the dataset metadata are, etc. It can also override
                // cloudcoder.properties configuration values, if desirned (e.g.,
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

        askIfMissing(config, "courseId", "Course id: ", keyboard);
        askIfMissing(config, "username", "Instructor username: ", keyboard);
        askIfMissing(config, "separationSeconds", "Session separation in seconds: ", keyboard);
        askIfMissing(config, "dest", "Progsnap2 output directory: ", keyboard);

        exporter.setConfig(config);

        File destDir = new File(config.getProperty("dest"));
        MainTableWriter mainTableWriter = new MainTableWriter(destDir);
        exporter.setMainTableWriter(mainTableWriter);

        // Do the export
        try {
            exporter.execute();
        } finally {
            IOUtils.closeQuietly(mainTableWriter);
        }
    }

    private static void askIfMissing(Properties config, String propName, String prompt, Scanner keyboard) {
        if (!config.containsKey(propName)) {
            config.setProperty(propName, Util.ask(keyboard, prompt));
        }
    }

    // TODO: Include version number and language student is using
    private static String[] TOOL_INSTANCES = { "CloudCoder" };
}