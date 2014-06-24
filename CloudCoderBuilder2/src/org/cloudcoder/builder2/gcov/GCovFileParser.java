// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.gcov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a gcov results file:
 * i.e., what you find in the .gcov file after gcov executes.
 * The result is line coverage data.
 * 
 * @author David Hovemeyer
 */
public class GCovFileParser {
	/**
	 * Callback for receiving coverage data for a source line.
	 */
	public interface LineDataCallback {
		/**
		 * Receive line coverage data.
		 * 
		 * @param lineNumber    the source line
		 * @param timesExecuted the number of times the source line was executed
		 */
		public void onLineData(int lineNumber, int timesExecuted);
	}
	
	private BufferedReader r;
	
	/**
	 * Constructor.
	 * 
	 * @param r the Reader to read gcov data from:
	 *        note that the GCovFileParser does <em>not</em> assume
	 *        responsibility for closing the Reader
	 */
	public GCovFileParser(Reader r) {
		this.r = new BufferedReader(r);
	}
	
	private static final Pattern LINE_DATA_REGEX =
			Pattern.compile("^\\s*(\\#+|\\d+)\\s*:\\s*(\\d+)\\s*:");

	/**
	 * Read coverage data.
	 * 
	 * @param callback callback to notify when coverage data is parsed
	 * @throws IOException
	 */
	public void parse(LineDataCallback callback) throws IOException {
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			Matcher m = LINE_DATA_REGEX.matcher(line);
			if (m.find()) {
				int lineNumber = Integer.parseInt(m.group(2));
				int timesExecuted = m.group(1).startsWith("#") ? 0 : Integer.parseInt(m.group(1));
				callback.onLineData(lineNumber, timesExecuted);
			}
		}
	}
	
	/*
	// Just for testing
	public static void main(String[] args) throws IOException {
		FileReader r = new FileReader("gcov.out");
		GCovFileParser parser = new GCovFileParser(r);
		parser.parse(new LineDataCallback() {
			@Override
			public void onLineData(int lineNumber, int timesExecuted) {
				System.out.printf("lineNumber=%d, timesExecuted=%d\n", lineNumber, timesExecuted);
			}
		});
		r.close();
	}
	*/
}
