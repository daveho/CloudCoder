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

import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.IProblemAndTestCaseData;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;

/**
 * Methods for converting CloudCoder model objects to and from XML.
 * 
 * @author David Hovemeyer
 */
public class XMLConversion {
	
	// Element names
	public static final String PROBLEM_AND_TEST_CASE_DATA = "exercise";
	
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
		
		writeModelObject(obj.getProblem(), IProblemData.SCHEMA, writer);

		for (ITestCaseData testCaseData : obj.getTestCaseData()) {
			writeModelObject(testCaseData, ITestCaseData.SCHEMA, writer);
		}
		
		writer.writeEndElement();
	}
	
	/**
	 * Write a model object as an XML element using its schema.
	 * The model object's schema's name is used as the element name.
	 * 
	 * @param obj    the model object to write
	 * @param writer the XMLStreamWriter
	 * @throws XMLStreamException
	 */
	public static<E extends IModelObject<E>> void writeModelObject(E obj, XMLStreamWriter writer) throws XMLStreamException {
		writeModelObject(obj, obj.getSchema(), writer);
	}

	/**
	 * Write a model object using given schema.
	 * The schema name is used as the element name.
	 * 
	 * @param obj     the model object to write
	 * @param schema  the schema to use
	 * @param writer  the XMLStreamWriter
	 * @throws XMLStreamException
	 */
	public static <E> void writeModelObject(E obj, ModelObjectSchema<? super E> schema, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(schema.getName());
		writeModelObjectFields(obj, schema, writer);
		writer.writeEndElement();
	}
	
	// Read model objects as XML

	/**
	 * Convenience method for skipping to the first (root) element in
	 * an XML document.
	 * 
	 * @param reader an XMLStreamReader that is positioned at the beginning of a document
	 * @throws XMLStreamException
	 */
	public static void skipToFirstElement(XMLStreamReader reader) throws XMLStreamException {
		while (reader.hasNext()) {
			int eventType = reader.next();
			if (eventType == XMLStreamReader.START_ELEMENT) {
				return;
			}
		}
		throw new XMLStreamException("Did not find element start");
	}

	/**
	 * Read an {@link IProblemAndTestCaseData} object. 
	 * 
	 * @param obj              the {@link IProblemAndTestCaseData} object to read
	 * @param problemFactory   factory for creating the {@link IProblemData} object
	 * @param testCaseFactory  factory for creating {@link ITestCaseData} objects
	 * @param reader           the XMLStreamReader
	 * @throws XMLStreamException			
	 */
	public static<ProblemDataType extends IProblemData, TestCaseDataType extends ITestCaseData> void readProblemAndTestCaseData(
			IProblemAndTestCaseData<ProblemDataType, TestCaseDataType> obj,
			IFactory<ProblemDataType> problemFactory,
			IFactory<TestCaseDataType> testCaseFactory,
			XMLStreamReader reader)
			throws XMLStreamException {
		expectElementStart(PROBLEM_AND_TEST_CASE_DATA, reader);
		
		ProblemDataType problem = problemFactory.create();
		obj.setProblem(problem);
		readModelObject(problem, IProblemData.SCHEMA, reader);
		
		skipToElementStartOrEnd(reader);
		
		// Until we haven't reached the end of this element...
		while (reader.getEventType() != XMLStreamReader.END_ELEMENT) {
			// Is it a nested element?
			if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
				// Is it a test case data element?
				if (reader.getLocalName().equals(ITestCaseData.SCHEMA.getName())) {
					TestCaseDataType testCase = testCaseFactory.create();
					readModelObject(testCase, ITestCaseData.SCHEMA, reader);
					obj.addTestCase(testCase);
				} else {
					// Unrecognized nested element: just skip it
					skipToEndElement(reader);
				}
			}
		}
		
		expectElementEnd(reader); // consume close tag
	}

	/**
	 * Read a model object from the current element using the model object's schema.
	 * The current element's name must match the model object's schema's name.
	 * 
	 * @param modelObj the model object
	 * @param reader   the XMLStreamReader
	 * @throws XMLStreamException
	 */
	public static<E extends IModelObject<E>> void readModelObject(E modelObj, XMLStreamReader reader) throws XMLStreamException {
		readModelObject(modelObj, modelObj.getSchema(), reader);
	}

	/**
	 * Read a model object from the current element using the given schema.
	 * The current element's name must match the schema's name.
	 * 
	 * @param modelObj the model object to read
	 * @param schema   the schema
	 * @param reader   the XMLStreamReader
	 * @throws XMLStreamException
	 */
	public static<E> void readModelObject(E modelObj, ModelObjectSchema<E> schema, XMLStreamReader reader)
			throws XMLStreamException {
		expectElementStart(schema.getName(), reader);
		readModelObjectFields(modelObj, schema, reader);
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
	
	/**
	 * Skip to the next START_ELEMENT or END_ELEMENT event.
	 * This is useful when parsing the contents of an element that
	 * might have a variable (possibly-zero) number of child elements.
	 * In that case, we want to be able to advance to a child element
	 * if there is one
	 *  
	 * @param reader the XMLStreamReader
	 * @throws XMLStreamException
	 */
	private static void skipToElementStartOrEnd(XMLStreamReader reader) throws XMLStreamException {
		while (true) {
			int eventType = reader.getEventType();
			if (eventType == XMLStreamReader.START_ELEMENT || eventType == XMLStreamReader.END_ELEMENT) {
				break;
			}
			if (!reader.hasNext()) {
				throw new XMLStreamException("Unexpected end of document");
			}
			reader.next();
		}
	}
	
	private static void expectElementStart(String elementName, XMLStreamReader reader) throws XMLStreamException {
		if (reader.getEventType() != XMLStreamReader.START_ELEMENT
				|| !reader.getLocalName().equals(elementName)) {
			throw new XMLStreamException("Expected the start of a " + elementName + " element");
		}
		
		// Skip to next event
		if (!reader.hasNext()) {
			throw new XMLStreamException("Unexpected end of input");
		}
		reader.next();
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
	
	/**
	 * Read model object fields until an END_ELEMENT event is reached.
	 * 
	 * @param modelObj  the model object
	 * @param schema    the model object's schema
	 * @param reader    the XMLStreamReader
	 * @throws XMLStreamException
	 */
	private static<E> void readModelObjectFields(E modelObj, ModelObjectSchema<E> schema, XMLStreamReader reader) throws XMLStreamException {
		while (true) {
			int eventType = reader.getEventType();
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
			
			// Advance to next event
			if (!reader.hasNext()) {
				throw new XMLStreamException("Unexpected end of input");
			}
			reader.next();
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
