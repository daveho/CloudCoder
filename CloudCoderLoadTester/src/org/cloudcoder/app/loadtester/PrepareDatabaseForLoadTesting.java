package org.cloudcoder.app.loadtester;

import java.util.Scanner;

import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.User;

/**
 * Prepare the webapp database for load testing.
 * Right now, this just creates test user accounts.
 * 
 * @author David Hovemeyer
 */
public class PrepareDatabaseForLoadTesting {
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		createTestUserAccounts(keyboard);
	}

	private static void createTestUserAccounts(Scanner keyboard)
			throws CloudCoderAuthenticationException {
		HostConfig hostConfig = HostConfigDatabase.forName("default");
		
		Client client = new Client(hostConfig);
		
		// Must be logged in using an instructor account
		System.out.print("Instructor username: ");
		String userName = keyboard.nextLine();
		System.out.print("Instructor password: ");
		String password = keyboard.nextLine();
		
		client.login(userName, password);
		
		System.out.print("Course name: ");
		String courseName = keyboard.nextLine();

		// Find the course and make sure user is really an instructor
		CourseAndCourseRegistration theCCR = null;
		
		CourseAndCourseRegistration[] courses = client.getRegisteredCourses();
		for (CourseAndCourseRegistration ccr : courses) {
			if (ccr.getCourse().getName().equals(courseName)) {
				theCCR = ccr;
				break;
			}
		}
		
		if (theCCR == null) {
			System.out.println("Could not find course " + courseName);
			System.exit(1);
		}
		if (!theCCR.getCourseRegistration().getRegistrationType().isInstructor()) {
			System.out.println("User is not an instructor");
			System.exit(1);
		}
		
		System.out.print("Start user number: ");
		int startUserNum = keyboard.nextInt();
		System.out.print("End user number: ");
		int endUserNum = keyboard.nextInt();
		
		for (int n = startUserNum; n <= endUserNum; n++) {
			User user = new User();
			String testUserName = "user" + n;
			user.setUsername(testUserName);
			user.setFirstname("Test");
			user.setLastname("User");
			user.setPasswordHash(testUserName); // set to plaintext when adding/registering a user
			user.setEmail(testUserName + "@unseen.edu");
			user.setConsent("");
			user.setWebsite("http://student.unseen.edu/~" + testUserName);
			
			System.out.println("Adding user " + testUserName);
			client.createUser(user, theCCR.getCourse(), CourseRegistrationType.STUDENT, 101);
		}
	}
}
