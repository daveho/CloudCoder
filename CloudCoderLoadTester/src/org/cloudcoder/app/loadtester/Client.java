package org.cloudcoder.app.loadtester;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;

import org.cloudcoder.app.client.rpc.EditCodeService;
import org.cloudcoder.app.client.rpc.GetCoursesAndProblemsService;
import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.QuizEndedException;
import org.cloudcoder.app.shared.model.User;

import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * A client that can communicate with a CloudCoder webapp via
 * GWT RPC.  Note that unlike normal GWT RPC, this client is
 * synchronous.
 * 
 * @author David Hovemeyer
 */
public class Client {
	private String protocol;
	private String host;
	private int port;
	private String contextPath;
	private HashMap<Class<?>, Object> serviceMap;
	private User user;
	private CookieManager cookieManager;

	/**
	 * Constructor.
	 * 
	 * @param protocol    protocol for CloudCoder webapp (e.g., "http" or "https")
	 * @param host        hostname of CloudCoder webapp
	 * @param port        port of CloudCoder webapp
	 * @param contextPath context path of CloudCoder webapp
	 */
	public Client(String protocol, String host, int port, String contextPath) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.contextPath = contextPath;
		this.serviceMap = new HashMap<Class<?>, Object>();
		this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
	}
	
	private<E extends RemoteService> E getService(Class<E> cls) {
		Object obj = serviceMap.get(cls);
		if (obj == null) {
			String relativePath = getRelativePath(cls);
			obj = SyncProxy.newProxyInstance(cls, protocol + "://" + host + ":" + port + "/" + contextPath + "/", relativePath, this.cookieManager);
			serviceMap.put(cls, obj);
		}
		return cls.cast(obj);
	}
	
	private static<E extends RemoteService> String getRelativePath(Class<E> svcClass) {
		RemoteServiceRelativePath p = svcClass.getAnnotation(RemoteServiceRelativePath.class);
		if (p == null) {
			throw new IllegalStateException(svcClass.getName() + " has no @RemoteServiceRelativePath annotation");
		}
		return p.value();
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
}
