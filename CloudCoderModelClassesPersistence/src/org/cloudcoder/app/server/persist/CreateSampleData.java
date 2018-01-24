// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012,2018 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;

/**
 * Create sample data.
 * 
 * @author David Hovemeyer
 */
public class CreateSampleData {

	/**
	 * Create a demo course.
	 * 
	 * @param conn  the database connection
	 * @param term  the term in which the course should be created
	 * @return the course id of the newly-created course
	 * @throws SQLException
	 */
	public static int createDemoCourse(Connection conn, Term term) throws SQLException {
		Course course = new Course();
		course.setName("CCDemo");
		course.setTitle("CloudCoder demo course");
		course.setTermId(term.getId());
		course.setTerm(term);
		course.setYear(2018);
		course.setUrl("http://cloudcoder.org/");
		DBUtil.storeModelObject(conn, course);
		return course.getId();
	}
	
	public static final String[] SAMPLE_EXERCISES = {
			"853f98340957b459d88eb7fa357a672fd93fd6c5.json", // it goes to 11
			"af73cc5d07d227723227a97bfb69615968a7f976.json", // hamster years
			"bc77c471d2a69114afa872d0b07f6cc31412c618.json", // Not like the others
			"0ccb942793ee14ba1609dd371a9c6ce01ab2ccdb.json", // countOdd
			"d40eafb5c31a84af74c0d518b85d4da18df2eb06.json", // Largest digit
			"86bc02a726f8b86bc49e53e9777590f56219776d.json", // countAB
			"05ca41d6881d0e476cc6e66210bbd025ce58dc94.json", // shiftChar
			"8a670605cdc700399e86e03a027f46eeea294190.json", // min_of_three
			
	};
	
	/**
	 * Get sample exercises to add to test course when the
	 * webapp database is created.
	 * 
	 * @return list of sample exercises
	 * @throws IOException
	 */
	public static List<ProblemAndTestCaseList> getSampleExercises() throws IOException {
		List<ProblemAndTestCaseList> result = new ArrayList<ProblemAndTestCaseList>();
		
		String pkgPath = "org/cloudcoder/app/server/persist";
		
		for (String ex : SAMPLE_EXERCISES) {
			String resName = pkgPath + "/res/" + ex;
			InputStream in = CreateSampleData.class.getClassLoader().getResourceAsStream(resName);
			if (in == null) {
				throw new RuntimeException("Could not find resource " + resName);
			}
			ProblemAndTestCaseList exercise = new ProblemAndTestCaseList();
			Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			JSONConversion.readProblemAndTestCaseData(
					exercise,
					ReflectionFactory.forClass(Problem.class),
					ReflectionFactory.forClass(TestCase.class),
					reader);
			result.add(exercise);
		}
		
		return result;
	}
}
