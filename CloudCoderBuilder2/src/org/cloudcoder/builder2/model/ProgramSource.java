// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.model;

/**
 * Artifact with the source program text.
 * 
 * @author David Hovemeyer
 */
public class ProgramSource {
	private String programText;
	private int prologueLength;
	private int epilogueLength;

	/**
	 * Constructor.
	 * 
	 * @param programText the source program text 
	 */
	public ProgramSource(String programText) {
		this.programText = programText;
		this.prologueLength = 0;
		this.epilogueLength = 0;
	}

	/**
	 * Constructor.
	 * 
	 * @param programText    the source program text
	 * @param prologueLength number of lines of automatically-generated code at beginning of file
	 * @param epilogueLength number of lines of automatically-generated code at end of file
	 */
	public ProgramSource(String programText, int prologueLength, int epilogueLength) {
		this.programText = programText;
		this.prologueLength = prologueLength;
		this.epilogueLength = epilogueLength;
	}
	
	/**
	 * @return the source program text
	 */
	public String getProgramText() {
		return programText;
	}
	
	/**
	 * @return number of lines of automatically-generated code at the beginning of the file
	 */
	public int getPrologueLength() {
		return prologueLength;
	}
	
	/**
	 * @return number of lines of automatically-generated code at the end of the file
	 */
	public int getEpilogueLength() {
		return epilogueLength;
	}
}
