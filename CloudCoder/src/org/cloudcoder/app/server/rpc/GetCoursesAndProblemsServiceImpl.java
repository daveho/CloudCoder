package org.cloudcoder.app.server.rpc;

import java.util.List;

import org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;

public class GetCoursesAndProblemsServiceImpl extends NetCoderServiceImpl
		implements GetCoursesAndProblemsService {
	private static final long serialVersionUID = 1L;

	@Override
	public Course[] getCourses() throws NetCoderAuthenticationException {
		// make sure the client has authenticated
		User user = checkClientIsAuthenticated();
		
		System.out.println("Loading courses for user " + user.getUserName());
		
		List<? extends Object[]> resultList = Database.getInstance().getCoursesForUser(user);
		
		Course[] result = new Course[resultList.size()];
		int count = 0;
		for (Object[] pair : resultList) {
			Course course = (Course) pair[0];
			Term term = (Term) pair[1];
			course.setTerm(term);
			result[count++] = course;
			
			System.out.println("Found course: " + course.getName() + ": " + course.getTitle());
		}
		
		return result;
	}

	@Override
	public Problem[] getProblems(Course course) throws NetCoderAuthenticationException {
		// Make sure user is authenticated
		User user = checkClientIsAuthenticated();

		List<Problem> resultList = Database.getInstance().getProblemsInCourse(user, course);
		for (Problem p : resultList) {
			System.out.println(p.getTestName() + " - " + p.getBriefDescription());
		}
		
		return resultList.toArray(new Problem[resultList.size()]);
	}

}
