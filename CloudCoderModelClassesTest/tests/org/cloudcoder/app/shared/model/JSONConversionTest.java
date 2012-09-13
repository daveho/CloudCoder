package org.cloudcoder.app.shared.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;
import org.junit.Before;
import org.junit.Test;

public class JSONConversionTest {
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testReadProblemAndTestCaseData() throws Exception {
		InputStream in = this.getClass().getResourceAsStream("testdata/exercise.json");
		try {
			Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			
			RepoProblemAndTestCaseList exercise = new RepoProblemAndTestCaseList();
			
			JSONConversion.readProblemAndTestCaseData(
					exercise,
					ReflectionFactory.forClass(RepoProblem.class),
					ReflectionFactory.forClass(RepoTestCase.class),
					reader);
			
			assertEquals(ProblemType.C_PROGRAM, exercise.getProblem().getProblemType());
			assertEquals("hello", exercise.getProblem().getTestname());
			assertEquals("Print hello, world", exercise.getProblem().getBriefDescription());
			assertTrue(exercise.getProblem().getDescription().startsWith("<p>Print a line with the following text:"));
			assertTrue(exercise.getProblem().getSkeleton().startsWith("#include <stdio.h>"));
			assertEquals(0, exercise.getProblem().getSchemaVersion());
			assertEquals("A. User", exercise.getProblem().getAuthorName());
			assertEquals("auser@cs.unseen.edu", exercise.getProblem().getAuthorEmail());
			assertEquals("http://cs.unseen.edu/~auser", exercise.getProblem().getAuthorWebsite());
			assertEquals(1345230040466L, exercise.getProblem().getTimestampUtc());
			assertEquals(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0, exercise.getProblem().getLicense());
			
			assertEquals(1, exercise.getTestCaseData().size());
			RepoTestCase testCase = exercise.getTestCaseData().get(0);
			
			assertEquals("hello", testCase.getTestCaseName());
			assertEquals("", testCase.getInput());
			assertEquals("^\\s*Hello\\s*,\\s*world\\s*$i", testCase.getOutput());
			assertEquals(false, testCase.isSecret());
		} finally {
			in.close();
		}
	}
}
