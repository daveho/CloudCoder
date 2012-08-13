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

package org.cloudcoder.app.shared.model.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.cloudcoder.app.shared.model.IProblemAndTestCaseData;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCaseData;

/**
 * Methods for converting CloudCoder model objects to and from XML.
 * 
 * @author David Hovemeyer
 */
public class XMLConversion {
	
	// Element names
	public static final String PROBLEM_AND_TEST_CASE_DATA = "problemandtestcasedata";
	public static final String PROBLEM_DATA = "problemdata";
	public static final String TEST_CASE_DATA = "testcasedata";
	
	/**
	 * Write an {@link IProblemAndTestCaseData} object as a single element.
	 * 
	 * @param obj    the {@link IProblemAndTestCaseData} object to write
	 * @param writer the XMLStreamWriter
	 * @throws XMLStreamException
	 */
	public static void writeProblemAndTestCaseData(
			IProblemAndTestCaseData<? extends IProblemData, ? extends ITestCaseData> obj,
			XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(PROBLEM_AND_TEST_CASE_DATA);
		
		writeProblemData(obj.getProblem(), writer);
		
		for (ITestCaseData testCaseData : obj.getTestCaseData()) {
			writeTestCaseData(testCaseData, writer);
		}
		
		writer.writeEndElement();
	}
	
	/**
	 * Write an {@link IProblemData} object as a single element.
	 * 
	 * @param problemData the {@link IProblemData} object to write
	 * @param writer      the XMLStreamWriter
	 * @throws XMLStreamException
	 */
	public static void writeProblemData(IProblemData problemData, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(PROBLEM_DATA);
		writeProblemDataFields(problemData, writer);
		writer.writeEndElement();
	}

	/**
	 * Write an {@link ITestCaseData} object as a single element.
	 * @param testCaseData the {@link ITestCaseData} object to write
	 * @param writer       the XMLStreamWriter
	 * @throws XMLStreamException
	 */
	public static void writeTestCaseData(ITestCaseData testCaseData, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(TEST_CASE_DATA);
		writeTestCaseDataFields(testCaseData, writer);
		writer.writeEndElement();
	}
	
	private static void writeProblemDataFields(IProblemData problemData, XMLStreamWriter writer) throws XMLStreamException {
		writeInt(problemData.getSchemaVersion(), "schemaversion", writer);
		writeString(problemData.getProblemType().toString(), "problemtype", writer);
		writeString(problemData.getBriefDescription(), "briefdescription", writer);
		writeStringCData(problemData.getDescription(), "description", writer);
		writeStringCData(problemData.getSkeleton(), "skeleton", writer);
		writeString(problemData.getAuthorName(), "authorname", writer);
		writeString(problemData.getAuthorEmail(), "authoremail", writer);
		writeString(problemData.getAuthorWebsite(), "authorwebsite", writer);
		writeString(problemData.getTestname(), "testname", writer);
		writeLong(problemData.getTimestampUtc(), "timestamp", writer);
		writeString(problemData.getLicense().toString(), "license", writer);
		//writeString(problemData.getParentHash(), "parenthash", writer);
	}
	
	private static void writeTestCaseDataFields(ITestCaseData testCaseData, XMLStreamWriter writer) throws XMLStreamException {
		writeString(testCaseData.getTestCaseName(), "name", writer);
		writeStringCData(testCaseData.getInput(), "input", writer);
		writeStringCData(testCaseData.getOutput(), "output", writer);
		writeBoolean(testCaseData.isSecret(), "secret", writer);
	}
	
	// Low-level data conversion to XML

	private static void writeInt(int value, String elementName, XMLStreamWriter writer) throws XMLStreamException {
		writeString(String.valueOf(value), elementName, writer);
	}

	private static void writeLong(long s, String elementName, XMLStreamWriter writer) throws XMLStreamException {
		writeString(String.valueOf(s), elementName, writer);
	}

	private static void writeBoolean(boolean value, String elementName, XMLStreamWriter writer) throws XMLStreamException {
		writeString(String.valueOf(value), elementName, writer);
	}

	private static void writeString(String s, String elementName, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(elementName);
		writer.writeCharacters(s);
		writer.writeEndElement();
	}
	
	private static void writeStringCData(String s, String elementName, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(elementName);
		writer.writeCData(s);
		writer.writeEndElement();
	}
}
