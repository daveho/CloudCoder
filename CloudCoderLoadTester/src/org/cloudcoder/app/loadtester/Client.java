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
import java.net.CookieManager;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.client.rpc.EditCodeService;
import org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService;
import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.client.rpc.SubmitService;
import org.cloudcoder.app.client.rpc.UserService;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.User;

import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * A client that can communicate with a CloudCoder webapp via
 * GWT RPC.  Note that unlike normal GWT RPC, this client is
 * synchronous.  Uses {@link LoadTesterCookieHandler} to
 * force per-thread cookie management.  This means that a single
 * Client object must only be used by one thread, but that
 * it is safe to use many Clients in many threads.
 * (Which, of course, is the whole point of load testing!)
 * 
 * @author David Hovemeyer
 * @see https://code.google.com/p/gwt-syncproxy/
 */
public class Client {
	/**
	 * A {@link CookieManager} that <em>always</em> delegates to
	 * {@link LoadTesterCookieHandler}, which in turn is guaranteed to
	 * a thread-local {@link CookieManager}.
	 */
	private static class ClientCookieManager extends CookieManager {
		@Override
		public Map<String, List<String>> get(URI uri,
				Map<String, List<String>> requestHeaders)
				throws IOException {
			return LoadTesterCookieHandler.getInstance().get(uri, requestHeaders);
		}

		@Override
		public void put(URI uri,
				Map<String, List<String>> responseHeaders)
				throws IOException {
			LoadTesterCookieHandler.getInstance().put(uri, responseHeaders);
		}
	}

	private HostConfig hostConfig;
	private HashMap<Class<?>, Object> serviceMap;
	private User user;
	
	/**
	 * Constructor.
	 * 
	 * @param hostConfig the {@link HostConfig} (how to connect to the webapp)
	 */
	public Client(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
		this.serviceMap = new HashMap<Class<?>, Object>();
	}
	
	private<E extends RemoteService> E getService(Class<E> cls) {
		Object obj = serviceMap.get(cls);
		if (obj == null) {
			obj = SyncProxy.newProxyInstance(cls, createModuleBaseUrl(), new ClientCookieManager());
			serviceMap.put(cls, obj);
		}
		return cls.cast(obj);
	}

	private String createModuleBaseUrl() {
		StringBuilder buf = new StringBuilder();
		
		buf.append(hostConfig.getProtocol());
		buf.append("://");
		buf.append(hostConfig.getHostname());
		if (hostConfig.getPort() > 0) {
			buf.append(":");
			buf.append(hostConfig.getPort());
		}
		buf.append("/");
		buf.append(hostConfig.getContextPath());
		buf.append("/");
		
		return buf.toString();
	}
	
	/**
	 * Get the logged-in {@link User}.
	 * This method may only be called after a successful call
	 * to the {@link #login(String, String)} method.
	 * 
	 * @return the logged-in {@link User}
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Log in.
	 * 
	 * @param username  the username
	 * @param password  the password
	 * @return true if login was successful, false otherwise
	 */
	public boolean login(String username, String password) {
		LoginService loginSvc = getService(LoginService.class);
		this.user = loginSvc.login(username, password);
		return user != null;
	}
	
	/**
	 * Get list of {@link CourseAndCourseRegistration}s for logged-in user.
	 * 
	 * @return list of {@link CourseAndCourseRegistration}s
	 * @throws CloudCoderAuthenticationException
	 */
	public CourseAndCourseRegistration[] getRegisteredCourses() throws CloudCoderAuthenticationException {
		GetCoursesAndProblemsService getCoursesAndProblemsSvc = getService(GetCoursesAndProblemsService.class);
		CourseAndCourseRegistration[] result = getCoursesAndProblemsSvc.getCourseAndCourseRegistrations();
		return result;
	}
	
	/**
	 * Get list of {@link Problem}s (exercises) available to the
	 * logged-in user in the given {@link Course}.
	 * 
	 * @param course the {@link Course}
	 * @return list of {@link Problem}s
	 * @throws CloudCoderAuthenticationException
	 */
	public Problem[] getProblemsForCourse(Course course) throws CloudCoderAuthenticationException {
		GetCoursesAndProblemsService getCoursesAndProblemsSvc = getService(GetCoursesAndProblemsService.class);
		Problem[] result = getCoursesAndProblemsSvc.getProblems(course);
		return result;
	}

	/**
	 * Set the {@link Problem}.  This is used to establish which
	 * exercise the user is working on.
	 * 
	 * @param problem the {@link Problem} to set
	 * @throws CloudCoderAuthenticationException 
	 */
	public void setProblem(Problem problem) throws CloudCoderAuthenticationException {
		EditCodeService editCodeSvc = getService(EditCodeService.class);
		editCodeSvc.setProblem(problem.getProblemId());
	}

	/**
	 * Send a batch of {@link Change}s.
	 * 
	 * @param arr batch of {@link Change}s to send
	 * @throws CloudCoderAuthenticationException
	 * @throws QuizEndedException
	 */
	public void sendChanges(Change[] arr) throws CloudCoderAuthenticationException, QuizEndedException {
		EditCodeService editCodeSvc = getService(EditCodeService.class);
		editCodeSvc.logChange(arr, System.currentTimeMillis());
	}

	/**
	 * Submit code.  The {@link #setProblem(Problem)} method must be called
	 * first to establish which problem (exercise) the client is working on.
	 * Note that the code submission API is inherently asynchronous, in that
	 * the client is expected to explicitly poll to get the {@link SubmissionResult}.
	 * This method does that polling.
	 * 
	 * @param problemId      the problem id
	 * @param code           the code to submit
	 * @param pollIntervalMs interval at which to poll to get the {@link SubmissionResult},
	 *                       in milliseconds
	 * @return the {@link SubmissionResult}
	 * @throws QuizEndedException 
	 * @throws SubmissionException 
	 * @throws CloudCoderAuthenticationException 
	 * @throws InterruptedException 
	 */
	public SubmissionResult submitCode(int problemId, String code, long pollIntervalMs)
			throws CloudCoderAuthenticationException, SubmissionException, QuizEndedException, InterruptedException {
		SubmitService submitSvc = getService(SubmitService.class);
		submitSvc.submit(problemId, code);
		while (true) {
			SubmissionResult result = null;
			result = submitSvc.checkSubmission();
			if (result != null) {
				return result;
			}
			Thread.sleep(pollIntervalMs);
		}
	}
	
	/**
	 * Add user to given couse, creating if necessary.
	 * 
	 * @param user     the user
	 * @param course   the course
	 * @param regType  the user's registration type
	 * @param section  the course section
	 * @throws CloudCoderAuthenticationException
	 */
	public void createUser(User user, Course course, CourseRegistrationType regType, int section) throws CloudCoderAuthenticationException {
		UserService userSvc = getService(UserService.class);
		
		EditedUser editedUser = new EditedUser();
		editedUser.setUser(user);
		editedUser.setPassword(user.getPasswordHash());
		editedUser.setRegistrationType(regType);
		editedUser.setSection(section);
		
		userSvc.addUserToCourse(editedUser, course.getId());
	}

	/**
	 * Loads the user's current text for the current problem
	 * (as set with previous call to {@link #setProblem(Problem)}.
	 * 
	 * @return the {@link ProblemText}
	 * @throws CloudCoderAuthenticationException
	 */
	public ProblemText loadCurrentText() throws CloudCoderAuthenticationException {
		EditCodeService editCodeSvc = getService(EditCodeService.class);
		return editCodeSvc.loadCurrentText();
	}
}
