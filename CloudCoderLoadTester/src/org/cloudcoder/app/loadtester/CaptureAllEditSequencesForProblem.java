// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.loadtester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.Problem;

/**
 * Capture all {@link EditSequence}s for all users' work on a
 * specified {@link Problem} (exercise).
 * 
 * @author David Hovemeyer
 */
public class CaptureAllEditSequencesForProblem {
	private int problemId;
	private String outputDir;
	private List<EditSequence> editSequenceList;
	
	public CaptureAllEditSequencesForProblem() {
		editSequenceList = new ArrayList<EditSequence>();
	}
	
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	
	public List<EditSequence> getEditSequenceList() {
		return editSequenceList;
	}
	
	public void capture() {
		// Load the Problem, in order to determine the exercise name
		Problem problem = new Problem();
		problem.setProblemId(problemId);
		Database.getInstance().reloadModelObject(problem);
		
		List<Change> allChanges = Database.getInstance().loadChangesForAllUsersOnProblem(problemId);
		
		EditSequence seq = null;
		int userId = -1;
		
		for (Change change : allChanges) {
			if (change.getEvent().getUserId() != userId) {
				// Make a new EditSequence
				seq = new EditSequence();
				seq.setExerciseName(problem.getTestname());
				seq.setChangeList(new ArrayList<Change>());
				userId = change.getEvent().getUserId();
				editSequenceList.add(seq);
			}
			seq.getChangeList().add(change);
		}
	}

	public void write() throws IOException {
		List<EditSequence> editSequenceList = this.getEditSequenceList();
		System.out.println("Captured " + editSequenceList.size() + " edit sequences");
		for (EditSequence seq : editSequenceList) {
			int userId = seq.getChangeList().get(0).getEvent().getUserId();
			System.out.print("User id=" + userId + ", ");
			System.out.println(seq.getChangeList().size() + " changes");

			// Files are named by user id
			String outFile = String.format("%s/%03d.dat", outputDir, userId);
			seq.saveToFile(outFile);
			System.out.println("Saved to file " + outFile);
		}
	}
	
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Problem id: ");
		int problemId = keyboard.nextInt();
		System.out.print("Output dir: " );
		String outputDir = keyboard.nextLine();
		execute(problemId, outputDir);
	}

	public static void execute(int problemId, String outputDir) throws IOException {
		Properties config = DBUtil.getConfigProperties();
		JDBCDatabaseConfig.createFromProperties(config);
		
		CaptureAllEditSequencesForProblem cesp = new CaptureAllEditSequencesForProblem();
		cesp.setProblemId(problemId);
		cesp.setOutputDir(outputDir);
		
		cesp.capture();
		cesp.write();
	}
}
