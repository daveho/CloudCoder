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

package org.cloudcoder.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Import a CloudCoder problem from an XML document.
 * 
 * @author David Hovemeyer
 */
public class ImportProblem extends UsesDatabase {
	private String problemXml;
	private int courseId;

	public ImportProblem(String configPropertiesFileName, String problemXml, int courseId) throws IOException {
		super(configPropertiesFileName);
		this.problemXml = problemXml;
		this.courseId = courseId;
	}
	
	@Override
	public void run() throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(problemXml);
		
		// Create and initialize Problem object
		Problem problem = new Problem();
		problem.setCourseId(courseId);
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
		
		// Insert the Problem into the database
		Database.getInstance().addProblem(problem);
		
		// Insert the TestCases into the database
		Database.getInstance().addTestCases(problem, testCaseList);
		
		System.out.println("Problem uploaded successfully!");
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: " + ImportProblem.class.getName() + " <config properties> <problem xml> <course id>");
			System.exit(1);
		}
		
		String configPropertiesFileName = args[0];
		String problemXml = args[1];
		int courseId = Integer.parseInt(args[2]);

		new ImportProblem(configPropertiesFileName, problemXml, courseId).run();
	}

	private static String getElementText(Branch doc, String xpath) {
		Node node = doc.selectSingleNode(xpath);
		if (node == null) {
			throw new IllegalArgumentException("No node found for " + xpath);
		}
		return node.getText();
	}
}
