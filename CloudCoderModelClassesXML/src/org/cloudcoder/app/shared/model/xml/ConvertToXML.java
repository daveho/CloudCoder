package org.cloudcoder.app.shared.model.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.IProblemDataWithTestCaseData;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.ProblemData;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * Methods for converting CloudCoder model objects to XML.
 * 
 * @author David Hovemeyer
 */
public class ConvertToXML {
	/**
	 * Write the fields of an {@link IProblemData} object to an XMLStreamWriter.
	 * Note that this does not create an entire element: it is meant to be called
	 * to generate ProblemData inside another element.
	 * 
	 * @param problemData  the {@link IProblemData} object whose fields should be written
	 * @param writer       the XMLStreamWriter to write to
	 * @throws XMLStreamException
	 */
	public static void writeProblemDataFields(IProblemData problemData, XMLStreamWriter writer) throws XMLStreamException {
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
	
	/**
	 * Write the fields of an {@link ITestCaseData} object
	 * to given XMLStreamWriter.
	 * 
	 * @param testCaseData the {@link ITestCaseData} whose fields should be written
	 * @param writer   the XMLStreamWriter to write to
	 * @throws XMLStreamException
	 */
	public static void writeTestCase(ITestCaseData testCaseData, XMLStreamWriter writer) throws XMLStreamException {
		writeString(testCaseData.getTestCaseName(), "name", writer);
		writeStringCData(testCaseData.getInput(), "input", writer);
		writeStringCData(testCaseData.getOutput(), "output", writer);
		writeBoolean(testCaseData.isSecret(), "secret", writer);
	}
	
	/**
	 * Write an {@link IProblemDataWithTestCaseData} as a complete element.
	 * The overall element has the tag <code>problem</code>, and each test
	 * case is nested in an element with the tag name <code>testcase</code>.
	 * 
	 * @param obj the {@link IProblemDataWithTestCaseData} object to write
	 * @param writer the XMLStreamWriter to write to
	 * @throws XMLStreamException
	 */
	public static void writeProblemDataWithTestCaseData(
			IProblemDataWithTestCaseData<? extends IProblemData, ? extends ITestCaseData> obj,
			XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("problem");
		
		writeProblemDataFields(obj.getProblem(), writer);
		
		for (ITestCaseData testCaseData : obj.getTestCaseData()) {
			writer.writeStartElement("testcase");
			writeTestCase(testCaseData, writer);
			writer.writeEndElement();
		}
		
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
