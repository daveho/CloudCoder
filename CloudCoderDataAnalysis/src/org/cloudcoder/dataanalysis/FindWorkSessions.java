// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.WorkSession;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Find "work sessions": series of events that are likely
 * to be part of the same work session.  Based on a maximum
 * separation, we group all events that are within that separation
 * of each other into a separate session.  The output includes
 * student, problem, start time, and finish time.
 * 
 * @author David Hovemeyer
 */
public class FindWorkSessions {
	public static void main(String[] args) throws IOException {
		Util.configureLogging();
		
		Properties config = new Properties();
		
		Scanner keyboard = new Scanner(System.in);
		Util.readDatabaseProperties(keyboard, config);
		Util.connectToDatabase(config);
		
		int courseId = Integer.parseInt(Util.ask(keyboard, "Course id: "));
		int separationSeconds = Integer.parseInt(Util.ask(keyboard, "Separation in seconds: "));
		String resultFileName = Util.ask(keyboard, "Result filename: ");
		
		final List<WorkSession> workSessions = Database.getInstance().findWorkSessions(courseId, separationSeconds);
		
		PrintWriter pw = new PrintWriter(new FileWriter(resultFileName));
		CSV csv = CSV
				.separator(',')  // delimiter of fields
				.quote('"')      // quote character
				.create();       // new instance is immutable
		csv.write(pw, new CSVWriteProc() {
			@Override
			public void process(CSVWriter w) {
				w.writeNext("courseId", "problemId", "userId", "startEventId", "endEventId", "startTime", "endTime");
				for (WorkSession ws : workSessions) {
					w.writeNext(
							String.valueOf(ws.getCourseId()),
							String.valueOf(ws.getProblemId()),
							String.valueOf(ws.getUserId()),
							String.valueOf(ws.getStartEventId()),
							String.valueOf(ws.getEndEventId()),
							String.valueOf(ws.getStartTime()),
							String.valueOf(ws.getEndTime())
					);
				}
			}
		});
		pw.close();
	}
}
