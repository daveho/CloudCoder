// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.dataanalysis;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Command name required");
			System.exit(1);
		}
		
		String app = args[0];
		String[] rest = new String[args.length - 1];
		System.arraycopy(args, 1, rest, 0, args.length - 1);
		
		if (app.equals("anonymize")) {
			Anonymize.main(rest);
		} else if (app.equals("findWorkSessions")) {
			FindWorkSessions.main(rest);
		} else if (app.equals("retest")) {
			Retest.main(rest);
		} else if (app.equals("attempts")) {
			Attempts.main(rest);
		} else if (app.equals("tts")) {
			TimeToSolve.main(rest);
		} else if (app.equals("pauseTimes")) {
			PauseTimes.main(rest);
		} else if (app.equals("export")) {
			ProgsnapExport.main(rest);
		} else {
			System.out.println("Unknown app name: " + app);
			System.exit(1);
		}
	}
}
