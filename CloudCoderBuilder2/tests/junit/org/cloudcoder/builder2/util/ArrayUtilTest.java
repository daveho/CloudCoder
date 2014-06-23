package org.cloudcoder.builder2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.cloudcoder.app.shared.model.ModelObjectUtil;
import org.junit.Before;
import org.junit.Test;

public class ArrayUtilTest {
	
	private String[] a1; // no null values
	private String[] a2; // two null values
	
	@Before
	public void setUp() {
		a1 = new String[]{ "a", "b", "c" };
		a2 = new String[]{ "p", null, "q", "r", null };
	}
	
	@Test
	public void testCountNullElements() {
		assertEquals(0, ArrayUtil.countNullElements(a1));
		assertEquals(2, ArrayUtil.countNullElements(a2));
	}
	
	@Test
	public void testStripNullElements() {
		String[] a1Strip = ArrayUtil.stripNullElements(a1);
		assertTrue(ModelObjectUtil.arrayEquals(a1, a1Strip));
		
		String[] a2Strip = ArrayUtil.stripNullElements(a2);
		assertTrue(ModelObjectUtil.arrayEquals(new String[]{"p", "q", "r"}, a2Strip));
	}
}
