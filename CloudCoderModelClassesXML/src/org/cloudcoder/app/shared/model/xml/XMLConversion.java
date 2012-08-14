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
import org.cloudcoder.app.shared.model.ProblemType;
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

		// TODO: figure out how to use ModelObjectSchema/ModelObjectField to read and set field values in a generic way
		
		/*
		expectElementStart(PROBLEM_DATA, reader);
		while (reader.hasNext()) {
			int eventType = reader.next();
			if (eventType == XMLStreamReader.END_ELEMENT) {
				break;
			} else if (eventType == XMLStreamReader.START_ELEMENT) {
				String elementName = reader.getLocalName();
				if (elementName.equals("schemaversion")) {
					problemData.setSchemaVersion(getElementTextAsInt(reader));
				} else if (elementName.equals("problemtype")) {
					problemData.setProblemType(getElementTextAsEnum(reader, ProblemType.class));
				} else if (elementName.equals("briefdescription")) {
					problemData.setBriefDescription(getElementTextTrimmed(reader));
				} else if (elementName.equals("description")) {
					problemData.setDescription(getElementTextTrimmed(reader));
				} else if (elementName.equals("skeleton")) {
					problemData.setSkeleton(getElementTextTrimmed(reader));
				} else if (elementName.equals("authorname")) {
					problemData.setAuthorName(getElementTextTrimmed(reader));
				} else if (elementName.equals("authoremail")) {
					problemData.setAuthorEmail(getElementTextTrimmed(reader));
				}
			} else {
				// Ignore other kinds of events
			}
		}
		*/
	}
	
	// Low-level data conversion to XML
	
	private static<E> void writeModelObjectFields(E modelObj, ModelObjectSchema<E> schema, XMLStreamWriter writer) throws XMLStreamException {
		// FIXME: need a way for ModelObjectField to convey that CDATA should be emitted instead of plain text
		
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			Object value = field.get(modelObj);
			writer.writeStartElement(field.getName());
			writer.writeCharacters(value.toString());
			writer.writeEndElement();
		}
	}

//	private static void writeInt(int value, String elementName, XMLStreamWriter writer) throws XMLStreamException {
//		writeString(String.valueOf(value), elementName, writer);
//	}
//
//	private static void writeLong(long s, String elementName, XMLStreamWriter writer) throws XMLStreamException {
//		writeString(String.valueOf(s), elementName, writer);
//	}
//
//	private static void writeBoolean(boolean value, String elementName, XMLStreamWriter writer) throws XMLStreamException {
//		writeString(String.valueOf(value), elementName, writer);
//	}
//
//	private static void writeString(String s, String elementName, XMLStreamWriter writer) throws XMLStreamException {
//		writer.writeStartElement(elementName);
//		writer.writeCharacters(s);
//		writer.writeEndElement();
//	}
//	
//	private static void writeStringCData(String s, String elementName, XMLStreamWriter writer) throws XMLStreamException {
//		writer.writeStartElement(elementName);
//		writer.writeCData(s);
//		writer.writeEndElement();
//	}
	
	// Low-level data conversion from XML
	
	private static void expectElementStart(String elementName, XMLStreamReader reader) throws XMLStreamException {
		if (reader.getEventType() != XMLStreamReader.START_ELEMENT) {
			throw new XMLStreamException("not at start of element");
		}
		if (!reader.getLocalName().equals(elementName)) {
			throw new XMLStreamException("Unexpected element (saw '" + reader.getLocalName() + "', expected '" + elementName + "'");
		}
	}

	private static int getElementTextAsInt(XMLStreamReader reader) throws XMLStreamException {
		String elementText = getElementTextTrimmed(reader);
		try {
			return Integer.parseInt(elementText);
		} catch (NumberFormatException e) {
			throw new XMLStreamException("Illegal integer value: " + elementText);
		}
	}

	private static<E extends Enum<E>> E getElementTextAsEnum(XMLStreamReader reader, Class<E> enumClass) throws XMLStreamException {
		String value = getElementTextTrimmed(reader);
		E[] members = enumClass.getEnumConstants();
		for (E member : members) {
			if (member.name().equals(value)) {
				return member;
			}
		}
		throw new XMLStreamException("Enum class " + enumClass.getSimpleName() + " has no member named " + value);
	}

	private static String getElementTextTrimmed(XMLStreamReader reader) throws XMLStreamException {
		return reader.getElementText().trim();
	}
}
