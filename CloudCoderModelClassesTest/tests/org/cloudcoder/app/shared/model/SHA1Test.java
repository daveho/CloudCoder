package org.cloudcoder.app.shared.model;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.junit.Before;
import org.junit.Test;

//
// Test that our SHA1 class behaves the same way as a java.util.MessageDigest
// with the SHA-1 algorithm.  The GWT emulated Java libraries don't support
// SHA-1 (as of version 2.4 anyway), so we have to provide our own
// implementation.
//
public class SHA1Test {
	private static final String ABC = "abc";
	private static final String ABCD_BLAH_BLAH = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
	private static final String GETTYSBURG = "Four score and seven years ago our fathers " +
			"brought forth on this continent a new nation, conceived in liberty, " +
			"and dedicated to the proposition that all men are created equal.";
	
	private SHA1 sha1;
	private Charset utf8;
	private MessageDigest oracle;
	
	@Before
	public void setUp() throws Exception {
		sha1 = new SHA1();
		utf8 = Charset.forName("UTF-8");
		oracle = MessageDigest.getInstance("SHA-1");
	}
	
	private String expected(String data) {
		byte[] digest = oracle.digest(data.getBytes(utf8));
		return new ConvertBytesToHex(digest).convert();
	}
	
	private String actual(String data) {
		byte[] digest = sha1.digest(data.getBytes(utf8));
		return new ConvertBytesToHex(digest).convert();
	}
	
	@Test
	public void testAbc() {
		assertEquals(expected(ABC), actual(ABC));
	}
	
	@Test
	public void testAbcdBlahBlah() {
		assertEquals(expected(ABCD_BLAH_BLAH), actual(ABCD_BLAH_BLAH));
	}
	
	@Test
	public void testGettysburg() {
		assertEquals(expected(GETTYSBURG), actual(GETTYSBURG));
	}
	
	@Test
	public void testUpdate() {
		// Make sure that hashes computed by repeated calls to update() work.
		
		byte[][] input = new byte[][]{
				ABC.getBytes(utf8),
				ABCD_BLAH_BLAH.getBytes(utf8),
				GETTYSBURG.getBytes(utf8),
		};
		
		for (byte[] chunk : input) {
			sha1.update(chunk);
			oracle.update(chunk);
		}
		
		assertEquals(
				new ConvertBytesToHex(sha1.digest()).convert(),
				new ConvertBytesToHex(oracle.digest()).convert());
	}
}
