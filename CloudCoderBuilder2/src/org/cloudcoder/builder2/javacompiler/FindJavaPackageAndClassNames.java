// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.builder2.javacompiler;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * Scan Java program text to determine package and class names.
 * 
 * @author David Hovemeyer
 */
public class FindJavaPackageAndClassNames {

	private enum Mode {
		SCAN,
		PACKAGE,
		CLASS,
	}

	private String packageName;
	private String className;
	
	/**
	 * Constructor.
	 */
	public FindJavaPackageAndClassNames() {
	}

	/**
	 * Scan given Java program text to find package and class names.
	 * 
	 * @param programText the Java program text to scan
	 */
	public void determinePackageAndClassNames(String programText) {
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(programText));
		
		tokenizer.parseNumbers();
		tokenizer.wordChars('_', '_');
		tokenizer.eolIsSignificant(true);
		tokenizer.slashSlashComments(true);
		tokenizer.slashStarComments(true);
		
		try {
		
			Mode mode = Mode.SCAN;
			StringBuilder pkgName = new StringBuilder();
			String clsName = null;
			
			while (true) {
				if (mode == Mode.SCAN && pkgName.length() > 0 && clsName != null) {
					break;
				}
				
				int token = tokenizer.nextToken();
				if (token == StreamTokenizer.TT_EOF) {
					break;
				}
				
				switch (mode) {
				case SCAN:
					if (token == StreamTokenizer.TT_WORD && tokenizer.sval.equals("package")) {
						mode = Mode.PACKAGE;
					} else if (token == StreamTokenizer.TT_WORD && tokenizer.sval.equals("class")) {
						mode = Mode.CLASS;
					}
					break;
					
				case PACKAGE:
					if (token == StreamTokenizer.TT_WORD) {
						pkgName.append(tokenizer.sval);
					} else if (token == '.') {
						pkgName.append(".");
					} else {
						mode = Mode.SCAN;
					}
					break;
					
				case CLASS:
					if (token == StreamTokenizer.TT_WORD) {
						clsName = tokenizer.sval;
					}
					mode = Mode.SCAN;
				}
			}
			

			this.packageName = pkgName.toString();
			this.className = clsName;
		
		} catch (IOException e) {
			throw new IllegalStateException("Can't happen");
		}
	}
	
	/**
	 * @return the package name
	 */
	public String getPackageName() {
		return packageName;
	}
	
	/**
	 * @return the bare top-level class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the fully-qualified top-level class name
	 */
	public String getFullyQualifiedClassName() {
		StringBuilder buf = new StringBuilder();
		
		if (!packageName.equals("")) {
			buf.append(packageName);
			buf.append(".");
		}
		buf.append(className);
		
		return buf.toString();
	}
}
