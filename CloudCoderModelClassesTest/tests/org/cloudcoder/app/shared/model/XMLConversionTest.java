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
		} finally {
			in.close();
		}
	}
}
