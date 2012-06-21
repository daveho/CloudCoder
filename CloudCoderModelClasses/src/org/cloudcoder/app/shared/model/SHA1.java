/*
 * A Java implementation of the Secure Hash Algorithm, SHA-1, as defined
 * in FIPS PUB 180-1
 * Copyright (C) Sam Ruby 2004
 * All rights reserved
 *
 * Based on code Copyright (C) Paul Johnston 2000 - 2002.
 * See http://pajhome.org.uk/site/legal.html for details.
 *
 * Converted to Java by Russell Beattie 2004
 * Base64 logic and inlining by Sam Ruby 2004
 * Bug fix correcting single bit error in base64 code by John Wilson
 * 
 * Changed so that input and output are byte arrays (removing Base64),
 *   added update(byte[]) and digest() methods to make it work
 *   more like java.security.MessageDigest, David Hovemeyer 2012
 *
 *                                BSD License
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.cloudcoder.app.shared.model;

import java.util.ArrayList;
import java.util.List;

public class SHA1 {
	private List<byte[]> inputDataList;
	
	/**
	 * Constructor.
	 */
	public SHA1() {
		this.inputDataList = new ArrayList<byte[]>();
	}

	/*
	 * Bitwise rotate a 32-bit number to the left
	 */
	private static int rol(int num, int cnt) {
		return (num << cnt) | (num >>> (32 - cnt));
	}
	
	/**
	 * Append some data to the message to be hashed.
	 * 
	 * @param data the data to add
	 */
	public void update(byte[] data) {
		inputDataList.add(data);
	}
	
	/**
	 * Return the SHA-1 hash of all of the data added using the
	 * {@link #update(byte[])} method.
	 *  
	 * @return SHA-1 hash of all data added with the update method
	 */
	public byte[] digest() {
		// Combine all chunks into a single array
		int totalNumBytes = 0;
		for (byte[] chunk : inputDataList) {
			totalNumBytes += chunk.length;
		}
		byte[] allData = new byte[totalNumBytes];
		int off = 0;
		for (byte[] chunk : inputDataList) {
			System.arraycopy(chunk, 0, allData, off, chunk.length);
			off += chunk.length;
		}
		
		return digest(allData);
	}

	/**
	 * Take an array of bytes and return the base64 representation of its SHA-1.
	 * Any data added to this object using the {@link #update(byte[])} method
	 * is ignored.
	 * 
	 * @param x the data to hash
	 * @return the SHA-1 hash of the data
	 */
	public byte[] digest(byte[] x) {

		// Convert a string to a sequence of 16-word blocks, stored as an array.
		// Append padding bits and the length, as described in the SHA1 standard

		int[] blks = new int[(((x.length + 8) >> 6) + 1) * 16];
		int i;

		for(i = 0; i < x.length; i++) {
			blks[i >> 2] |= x[i] << (24 - (i % 4) * 8);
		}

		blks[i >> 2] |= 0x80 << (24 - (i % 4) * 8);
		blks[blks.length - 1] = x.length * 8;

		// calculate 160 bit SHA1 hash of the sequence of blocks

		int[] w = new int[80];

		int a =  1732584193;
		int b = -271733879;
		int c = -1732584194;
		int d =  271733878;
		int e = -1009589776;

		for(i = 0; i < blks.length; i += 16) {
			int olda = a;
			int oldb = b;
			int oldc = c;
			int oldd = d;
			int olde = e;

			for(int j = 0; j < 80; j++) {
				w[j] = (j < 16) ? blks[i + j] :
					( rol(w[j-3] ^ w[j-8] ^ w[j-14] ^ w[j-16], 1) );

				int t = rol(a, 5) + e + w[j] +
						( (j < 20) ?  1518500249 + ((b & c) | ((~b) & d))
								: (j < 40) ?  1859775393 + (b ^ c ^ d)
										: (j < 60) ? -1894007588 + ((b & c) | (b & d) | (c & d))
												: -899497514 + (b ^ c ^ d) );
				e = d;
				d = c;
				c = rol(b, 30);
				b = a;
				a = t;
			}

			a = a + olda;
			b = b + oldb;
			c = c + oldc;
			d = d + oldd;
			e = e + olde;
		}

		// Convert result to a byte array
		byte[] digest = new byte[20];
		fill(a, digest, 0);
		fill(b, digest, 4);
		fill(c, digest, 8);
		fill(d, digest, 12);
		fill(e, digest, 16);

		return digest;
	}

	private void fill(int value, byte[] arr, int off) {
		arr[off + 0] = (byte) ((value >> 24) & 0xff);
		arr[off + 1] = (byte) ((value >> 16) & 0xff);
		arr[off + 2] = (byte) ((value >> 8) & 0xff);
		arr[off + 3] = (byte) ((value >> 0) & 0xff);
	}
}
