package org.cloudcoder.importer;

import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class ProblemReader {
	public ProblemWithTestCases read(Reader r) throws DocumentException, ParseException {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(r);
		
		// Create and initialize Problem object
		Problem problem = new Problem();
//		problem.setCourseId(courseId);
		problem.setProblemType(ProblemType.valueOf(getElementText(doc, "/ccproblem/type")));
		problem.setTestName(getElementText(doc, "/ccproblem/name"));
		problem.setBriefDescription(getElementText(doc, "/ccproblem/brief"));
		problem.setDescription(getElementText(doc, "/ccproblem/description"));
		problem.setWhenAssigned(DateTimeToMillis.convert(getElementText(doc, "/ccproblem/assigned")));
		problem.setWhenDue(DateTimeToMillis.convert(getElementText(doc, "/ccproblem/due")));
		Node skeletonNode = doc.selectSingleNode("/ccproblem/skeleton");
		if (skeletonNode != null) {
			problem.setSkeleton(skeletonNode.getText());
		}
		
		// Create and initialize TestCase objects
		List<TestCase> testCaseList = new ArrayList<TestCase>();
		List<?> tcElts = doc.selectNodes("/ccproblem/testcase");
		for (Object elt_ : tcElts) {
			Element elt = (Element) elt_;
			
			TestCase testCase = new TestCase();
			
			Node secretAttr = elt.selectSingleNode("./@secret");
			if (secretAttr != null) {
				testCase.setSecret(Boolean.valueOf(secretAttr.getText()));
			}
			
			testCase.setTestCaseName(getElementText(elt, "./name"));
			testCase.setInput(getElementText(elt, "./input"));
			testCase.setOutput(getElementText(elt, "./output"));
			
			testCaseList.add(testCase);
		}
		
		return new ProblemWithTestCases(problem, testCaseList);
	}

	private static String getElementText(Branch doc, String xpath) {
		Node node = doc.selectSingleNode(xpath);
		if (node == null) {
			throw new IllegalArgumentException("No node found for " + xpath);
		}
		return node.getText();
	}
}
