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
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.cloudcoder.app.shared.model.IProblemAndTestCaseData;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.ProblemData;
import org.cloudcoder.app.shared.model.TestCaseData;

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
	
	// Write model objects as XML
	
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
		writeModelObjectFields(problemData, ProblemData.SCHEMA, writer);
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
		writeModelObjectFields(testCaseData, TestCaseData.SCHEMA, writer);
		writer.writeEndElement();
	}
	
	// Read model objects as XML
	
	/**
	 * Read an {@link IProblemData} element from given XMLStreamReader,
	 * which must be positioned at the start of an element written by
	 * {@link #writeProblemData(IProblemData, XMLStreamWriter)}.
	 * 
	 * @param problemData
	 * @param reader
	 * @throws XMLStreamException 
	 */
	public static void readProblemData(IProblemData problemData, XMLStreamReader reader) throws XMLStreamException {
		expectElementStart(PROBLEM_DATA, reader);
		readModelObjectFields(problemData, ProblemData.SCHEMA, reader);
		expectElementEnd(reader);
	}
	
	// Low-level data conversion to XML

	private static<E> void writeModelObjectFields(E modelObj, ModelObjectSchema<E> schema, XMLStreamWriter writer) throws XMLStreamException {
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			Object value = field.get(modelObj);
			writer.writeStartElement(field.getName());
			if (field.getType() == String.class && field.isLiteral()) {
				// Emit as CDATA
				writer.writeCData(value.toString());
			} else {
				// Emit as normal text
				writer.writeCharacters(value.toString());
			}
			writer.writeEndElement();
		}
	}
	
	// Low-level data conversion from XML
	
	private static void expectElementStart(String elementName, XMLStreamReader reader) throws XMLStreamException {
		if (reader.getEventType() != XMLStreamReader.START_ELEMENT
				|| !reader.getLocalName().equals(elementName)) {
			throw new XMLStreamException("Expected the start of a " + elementName + " element");
		}
	}

	private static void expectElementEnd(XMLStreamReader reader) throws XMLStreamException {
		if (reader.getEventType() != XMLStreamReader.END_ELEMENT) {
			throw new XMLStreamException("Expected end of element");
		}
		// Advance to next event
		if (reader.hasNext()) {
			reader.next();
		}
	}
	
	private static<E> void readModelObjectFields(E modelObj, ModelObjectSchema<E> schema, XMLStreamReader reader) throws XMLStreamException {
		while (reader.hasNext()) {
			int eventType = reader.next();
			if (eventType == XMLStreamReader.END_ELEMENT) {
				break;
			} else if (eventType == XMLStreamReader.START_ELEMENT) {
				// See if it is a known field type
				String elementName = reader.getLocalName();
				ModelObjectField<? super E, ?> field = schema.getFieldByName(elementName);
				if (field != null) {
					// Read element text
					String elementText = reader.getElementText();
					
					// Decode as field type
					Object value = decodeString(elementText, field.getType());
					
					// Set field value in model object
					field.setUntyped(modelObj, value);
				} else {
					// Ignore the element by skipping to the corresponding END_ELEMENT event
					skipToEndElement(reader);
				}
			}
			// ignore other event types...
		}
	}

	private static Object decodeString(String elementText, Class<?> type) throws XMLStreamException {
		try {
			if (type == String.class) {
				return elementText;
			} else if (type == Integer.class) {
				elementText = elementText.trim();
				return Integer.parseInt(elementText);
			} else if (type == Long.class) {
				elementText = elementText.trim();
				return Long.parseLong(elementText);
			} else if (type == Boolean.class) {
				elementText = elementText.trim();
				return Boolean.parseBoolean(elementText);
			} else if (type.isEnum()) {
				elementText = elementText.trim();
				for (Object member_ : type.getEnumConstants()) {
					Enum<?> member = (Enum<?>) member_;
					if (member.name().equals(elementText)) {
						return member;
					}
				}
				throw new XMLStreamException("Value " + elementText + " is not a member of " + type.getName());
			} else {
				// TODO: other field types?
				throw new XMLStreamException("Cannot convert string to value of type " + type.getName());
			}
		} catch (NumberFormatException e) {
			throw new XMLStreamException("Could not convert " + elementText + " to a number", e);
		}
	}

	private static void skipToEndElement(XMLStreamReader reader) throws XMLStreamException {
		int depth = 0;
		while (reader.hasNext()) {
			int eventType = reader.next();
			if (eventType == XMLStreamReader.END_ELEMENT) {
				if (depth == 0) {
					return;
				} else {
					depth--;
				}
			} else if (eventType == XMLStreamReader.START_ELEMENT) {
				depth++;
			}
		}
	}
}
