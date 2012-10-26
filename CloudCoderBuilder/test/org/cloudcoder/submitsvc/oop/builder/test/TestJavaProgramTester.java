package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.submitsvc.oop.builder.JavaProgramTester;
import org.junit.Before;
import org.junit.Test;

public class TestJavaProgramTester {
	private static final String PROGRAM_TEXT = "package edu.ycp.acm.idcodes;\n" +
			"\n" +
			"import java.util.Scanner;\n" +
			"\n" +
			"public class Main {\n" +
			"	public static void main(String[] args) {\n";

	private JavaProgramTester tester;
	private Problem problem;
	private TestCase testCase;
	private Submission submission;

	@Before
	public void setUp() {
		tester = new JavaProgramTester();

	}

	@Test
	public void testFindPackageAndClassNames() {

	}
}
