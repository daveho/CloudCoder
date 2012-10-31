package org.cloudcoder.importer;

import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;

public class ProblemWithTestCases {
	private final Problem problem;
	private final List<TestCase> testCaseList;
	
	public ProblemWithTestCases(Problem problem, List<TestCase> testCaseList) {
		this.problem = problem;
		this.testCaseList = testCaseList;
	}
	
	public Problem getProblem() {
		return problem;
	}
	
	public List<TestCase> getTestCaseList() {
		return testCaseList;
	}
}
