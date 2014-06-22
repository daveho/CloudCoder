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

import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.SnapshotCallback;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;

/**
 * Make submissions/snapshots drawn from the database available for retesting.
 * Acts as a submission queue, so any number of builders on any
 * number of machines can be used to do the retesting.
 * 
 * @author David Hovemeyer
 */
public class Retest {
	private SnapshotSelectionCriteria criteria;
	
	public Retest() {
		
	}
	
	public void setCriteria(SnapshotSelectionCriteria criteria) {
		this.criteria = criteria;
	}
	
	public void execute() {
		Database.getInstance().retrieveSnapshots(criteria, new SnapshotCallback() {
			@Override
			public void onSnapshotFound(int submitEventId, int fullTextChangeId, int courseId, int problemId, int userId, String programText) {
				// FIXME just for testing
				System.out.printf("submitEventId=%d,fullTextChangeId=%d,courseId=%d,problemId=%d,userId=%d\n",
						submitEventId, fullTextChangeId, courseId, problemId, userId);
			}
		});
	}
	
	public static void main(String[] args) {
		boolean interactive = false;
		
		for (String arg : args) {
			if (arg.equals("--interactive")) {
				interactive = true;
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		
		Scanner keyboard = new Scanner(System.in);
		Util.configureLogging();
		Properties config = new Properties();
		if (interactive) {
			Util.readDatabaseProperties(keyboard, config);
		} else {
			ClassLoader classLoader = Retest.class.getClassLoader();
			Util.loadEmbeddedConfig(config, classLoader);
		}
		Util.connectToDatabase(config);
		Retest retest = new Retest();
		SnapshotSelectionCriteria criteria = new SnapshotSelectionCriteria();
		criteria.setCourseId(Integer.parseInt(Util.ask(keyboard, "Course id: ")));
		criteria.setProblemId(Integer.parseInt(Util.ask(keyboard, "Problem id: ")));
		criteria.setUserId(Integer.parseInt(Util.ask(keyboard, "User id: ")));
		retest.setCriteria(criteria);
		retest.execute();
	}
}
