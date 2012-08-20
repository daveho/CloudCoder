// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;

public class CreateCourse {
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		
		Class.forName("com.mysql.jdbc.Driver");
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		
		System.out.println("Create a CloudCoder course");

		// Get list of terms
		List<Term> terms = DBUtil.getAllModelObjects(conn, Term.SCHEMA, new IFactory<Term>() {
			@Override
			public Term create() {
				return new Term();
			}
		});
		
		Term term = choose(keyboard, "What term?", terms);
		int year = askInt(keyboard, "What year? ");
		String name = askString(keyboard, "Course name (e.g., \"CS 101\")? ");
		String title = askString(keyboard, "Course title (e.g., \"Introduction to Computer Science\")? ");
		String url = askString(keyboard, "Course URL? ");
		
		String instructorUsername = askString(keyboard, "Username of course instructor? ");
		User instructor = findUser(conn, instructorUsername);
		
		int section = askInt(keyboard, "What section is this instructor teaching? ");
		
		// FIXME: need to allow multiple sections and multiple instructors
		
		// Create the course
		Course course = new Course();
		course.setName(name);
		course.setTitle(title);
		course.setUrl(url);
		course.setTermId(term.getId());
		course.setYear(year);
		DBUtil.storeModelObject(conn, course);
		
		// Register the instructor
		CourseRegistration reg = new CourseRegistration();
		reg.setCourseId(course.getId());
		reg.setUserId(instructor.getId());
		reg.setRegistrationType(CourseRegistrationType.INSTRUCTOR);
		reg.setSection(section);
		DBUtil.storeModelObject(conn, reg);
		
		System.out.println("Success!");
	}

	static<E> E choose(Scanner keyboard, String prompt, List<E> values) {
		System.out.println(prompt);
		int count = 0;
		for (E val : values) {
			System.out.println((count++) + " - " + val);
		}
		System.out.print("[Enter value in range 0.." + (values.size()-1) + "] ");
		int choice = Integer.parseInt(keyboard.nextLine().trim());
		return values.get(choice);
	}
	
	
	private static int askInt(Scanner keyboard, String prompt) {
		System.out.print(prompt);
		return Integer.parseInt(keyboard.nextLine().trim());
	}


	private static String askString(Scanner keyboard, String prompt) {
		System.out.print(prompt);
		return keyboard.nextLine();
	}


	private static User findUser(Connection conn, String username) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = conn.prepareStatement("select * from " + User.SCHEMA.getDbTableName() + " where username = ?");
			stmt.setString(1, username);
			
			resultSet= stmt.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("No such user: " + username);
			}
			
			User user = new User();
			DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
			return user;
			
		} finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
		}
	}

}
