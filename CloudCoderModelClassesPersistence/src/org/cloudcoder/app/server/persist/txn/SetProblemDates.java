package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.User;

public class SetProblemDates extends AbstractDatabaseRunnableNoAuthException<OperationResult> {

	private User authenticatedUser;
	private Problem[] problems;

	public SetProblemDates(User authenticatedUser, Problem[] problems) {
		this.authenticatedUser = authenticatedUser;
		this.problems = problems;
	}

	@Override
	public OperationResult run(Connection conn) throws SQLException {
		if (problems != null && problems.length > 0) {
			// Make sure that all problems have the same course id
			int courseId = -1;
			for (Problem problem : problems) {
				if (courseId != -1 && problem.getCourseId() != courseId) {
					return new OperationResult(false, "All exercises must be from the same course");
				}
				courseId = problem.getCourseId();
			}
		
			// Find out whether the authenticated user is an instructor in the course
			CourseRegistrationList regList =
					Queries.doGetCourseRegistrations(conn, courseId, authenticatedUser.getId(), this);
			if (!regList.isInstructor()) {
				return new OperationResult(false, "Only an instructor can modify exercises");
			}
			
			// Do the update!
			PreparedStatement stmt = prepareStatement(
					conn,
					"update cc_problems set when_assigned = ?, when_due = ? where problem_id = ?"
			);
			for (Problem problem : problems) {
				stmt.setLong(1, problem.getWhenAssigned());
				stmt.setLong(2, problem.getWhenDue());
				stmt.setInt(3, problem.getProblemId());
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
		
		return new OperationResult(true, "Successfully updated dates/times of exercise(s)");
	}

	@Override
	public String getDescription() {
		return " setting when assigned/when due dates/times for problems";
	}

}
