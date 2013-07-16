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
