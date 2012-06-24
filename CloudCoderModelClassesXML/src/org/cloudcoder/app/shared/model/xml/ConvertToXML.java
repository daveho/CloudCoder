package org.cloudcoder.app.shared.model.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.cloudcoder.app.shared.model.ProblemData;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * Methods for converting CloudCoder model objects to XML.
 * 
 * @author David Hovemeyer
 */
public class ConvertToXML {
	/**
	 * Write the fields of a {@link ProblemData} object to an XMLStreamWriter.
	 * Note that this does not create an entire element: it is meant to be called
	 * to generate ProblemData inside another element.
	 * 
	 * @param problemData  the ProblemData object whose fields should be written
	 * @param writer       the XMLStreamWriter to write to
	 * @throws XMLStreamException
	 */
	public static void writeProblemDataFields(ProblemData problemData, XMLStreamWriter writer) throws XMLStreamException {
		writeInt(problemData.getSchemaVersion(), "schemaversion", writer);
		writeString(problemData.getProblemType().toString(), "problemtype", writer);
		writeString(problemData.getBriefDescription(), "briefdescription", writer);
		writeStringCData(problemData.getDescription(), "description", writer);
		writeStringCData(problemData.getSkeleton(), "skeleton", writer);
		writeString(problemData.getAuthorName(), "authorname", writer);
		writeString(problemData.getAuthorEmail(), "authoremail", writer);
		writeString(problemData.getAuthorWebsite(), "authorwebsite", writer);
		writeString(problemData.getTestName(), "testname", writer);
		writeLong(problemData.getTimestampUTC(), "timestamp", writer);
		writeString(problemData.getLicense().toString(), "license", writer);
		writeString(problemData.getParentHash(), "parenthash", writer);
	}
	
	/**
	 * Write a {@link TestCase} to given XMLStreamWriter as a complete element.
	 * 
	 * @param testCase the TestCase to write
	 * @param writer   the XMLStreamWriter to write to
	 * @throws XMLStreamException
	 */
	public static void writeTestCase(TestCase testCase, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("testcase");
		
		writeString(testCase.getTestCaseName(), "name", writer);
		writeStringCData(testCase.getInput(), "input", writer);
		writeStringCData(testCase.getOutput(), "output", writer);
		writeBoolean(testCase.isSecret(), "secret", writer);
		
		writer.writeEndElement();
	}

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
