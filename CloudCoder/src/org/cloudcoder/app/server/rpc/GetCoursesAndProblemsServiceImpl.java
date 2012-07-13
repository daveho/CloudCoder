package org.cloudcoder.app.server.rpc;

import java.util.List;

import org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GetCoursesAndProblemsServiceImpl extends RemoteServiceServlet
		implements GetCoursesAndProblemsService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(GetCoursesAndProblemsServiceImpl.class);

	@Override
	public Course[] getCourses() throws NetCoderAuthenticationException {
		// make sure the client has authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		
		logger.info("Loading courses for user " + user.getUserName());
		
		List<? extends Object[]> resultList = Database.getInstance().getCoursesForUser(user);
		
		Course[] result = new Course[resultList.size()];
		int count = 0;
		for (Object[] tuple : resultList) {
			Course course = (Course) tuple[0];
			Term term = (Term) tuple[1];
			course.setTerm(term);
			result[count++] = course;
			
			logger.info("Found course: " + course.getName() + ": " + course.getTitle());
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService#getCourseAndCourseRegistrations()
	 */
	@Override
	public CourseAndCourseRegistration[] getCourseAndCourseRegistrations() throws NetCoderAuthenticationException {
		// make sure the client has authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		
		logger.info("Loading courses and registrations for user " + user.getUserName());
		
		List<? extends Object[]> resultList = Database.getInstance().getCoursesForUser(user);
		
		CourseAndCourseRegistration[] result = new CourseAndCourseRegistration[resultList.size()];
		int count = 0;
		for (Object[] tuple : resultList) {
			Course course = (Course) tuple[0];
			Term term = (Term) tuple[1];
			course.setTerm(term);
			CourseRegistration reg = (CourseRegistration) tuple[2];
			
			CourseAndCourseRegistration obj = new CourseAndCourseRegistration();
			obj.setCourse(course);
			obj.setCourseRegistration(reg);
			
			result[count++] = obj;
		}
		
		return result;
	}

	@Override
	public Problem[] getProblems(Course course) throws NetCoderAuthenticationException {
		// Make sure user is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());

		List<Problem> resultList = Database.getInstance().getProblemsInCourse(user, course).getProblemList();
		for (Problem p : resultList) {
			logger.info(p.getTestName() + " - " + p.getBriefDescription());
		}
		
		return resultList.toArray(new Problem[resultList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService#getProblemAndSubscriptionReceipts(org.cloudcoder.app.shared.model.Course)
	 */
	@Override
	public ProblemAndSubmissionReceipt[] getProblemAndSubscriptionReceipts(
			Course course) throws NetCoderAuthenticationException {
		// Make sure user is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		
		List<ProblemAndSubmissionReceipt> resultList = Database.getInstance().getProblemAndSubscriptionReceiptsInCourse(user, course);
		return resultList.toArray(new ProblemAndSubmissionReceipt[resultList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService#getTestCasesForProblem(int)
	 */
	@Override
	public TestCase[] getTestCasesForProblem(int problemId) throws NetCoderAuthenticationException {
		// Make sure user is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());

		return Database.getInstance().getTestCasesForProblem(user, problemId);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService#storeProblemAndTestCaseList(org.cloudcoder.app.shared.model.ProblemAndTestCaseList)
	 */
	@Override
	public ProblemAndTestCaseList storeProblemAndTestCaseList(ProblemAndTestCaseList problemAndTestCaseList, Course course)
			throws NetCoderAuthenticationException {
		// Make sure user is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		
		Database.getInstance().storeProblemAndTestCaseList(problemAndTestCaseList, course, user);

		// TODO: implement
		ProblemAndTestCaseList copy = new ProblemAndTestCaseList();
		copy.copyFrom(problemAndTestCaseList);
		return copy;
	}
}
