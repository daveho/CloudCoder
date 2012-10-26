package org.cloudcoder.app.shared.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ProblemTest {
	private Problem problem;
	
	@Before
	public void setUp() {
		problem = new Problem();
	}
	
	@Test
	public void testSetTestname() {
		problem.setTestname("hello");
		assertEquals("hello", problem.getTestname());
	}
}
