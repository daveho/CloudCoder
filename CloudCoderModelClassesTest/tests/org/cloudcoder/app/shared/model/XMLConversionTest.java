package org.cloudcoder.app.shared.model;

import static org.junit.Assert.*;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.cloudcoder.app.shared.model.xml.XMLConversion;
import org.junit.Before;
import org.junit.Test;

public class XMLConversionTest {
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testReadProblemAndTestCaseData() throws Exception {
		InputStream in = this.getClass().getResourceAsStream("testdata/exercise.xml");
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(in);
			
			RepoProblemAndTestCaseList exercise = new RepoProblemAndTestCaseList();
			
			XMLConversion.skipToFirstElement(reader);
			XMLConversion.readProblemAndTestCaseData(
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
			assertEquals(1345042044837L, exercise.getProblem().getTimestampUtc());
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
