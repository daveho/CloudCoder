// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.loadtester;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Database of {@link Mix}es for load testing.
 * 
 * @author David Hovemeyer
 */
public class MixDatabase {
	private static final Map<String, Mix> MIX_MAP = new HashMap<String, Mix>();
	static {
		EditSequence countAB = load("86bc02a726f8b86bc49e53e9777590f56219776d");
		EditSequence dayNumbers = load("b5a9968781e7c4f602e66cfde6ded9745b06eff7");
		EditSequence whichAndHowMany = load("b89ba215e53343923a07d005cb03116ae07a31fb");
		
		MIX_MAP.put("default", new Mix().add(whichAndHowMany).add(dayNumbers).add(countAB));

		Mix skip3 = loadStudentData(4, 143, "6c0ba8b618beb177ef68588777287a30da1b02b0");
		MIX_MAP.put("skip3", skip3);
	}

	/**
	 * Load captured student data.
	 * 
	 * @param minUserId     minimum user id
	 * @param maxUserId     maximum user id (inclusive)
	 * @param exerciseHash  the exercise hash
	 * @return the {@link Mix} containing the captured student data
	 */
	private static Mix loadStudentData(int minUserId, int maxUserId,
			String exerciseHash) {
		Mix skip3 = new Mix();
		for (int i = minUserId; i <= maxUserId; i++) {
			try {
				String name = String.format(exerciseHash + "/%03d", i);
				EditSequence seq = load(name);
				skip3.add(seq);
			} catch (RuntimeException e) {
				// ignore: a student who didn't take the quiz
			}
		}
		return skip3;
	}
	
	private static EditSequence load(String hash) {
		try {
			InputStream in = MixDatabase.class.getClassLoader().getResourceAsStream(
					"org/cloudcoder/app/loadtester/res/" + hash + ".dat");
			EditSequence result = new EditSequence();
			result.loadFromInputStream(in);
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Could not load edit sequence " + hash, e);
		}
	}
	
	/**
	 * Get a named mix.
	 * 
	 * @param name the name
	 * @return the {@link Mix} with that name
	 */
	public static Mix forName(String name) {
		Mix result = MIX_MAP.get(name);
		if (result == null) {
			throw new IllegalArgumentException("Unknown mix name: " + name);
		}
		return result;
	}
}
