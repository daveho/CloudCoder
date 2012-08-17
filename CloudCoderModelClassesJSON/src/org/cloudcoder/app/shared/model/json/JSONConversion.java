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

package org.cloudcoder.app.shared.model.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.IProblemAndTestCaseData;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCase;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Methods for converting CloudCoder model objects to and from JSON.
 * 
 * @author David Hovemeyer
 */
public class JSONConversion {
	
	/**
	 * Write an {@link IProblemAndTestCaseData} object as a JSON array.
	 * The first element will be the {@link IProblemData}, and the
	 * subsequent objects will be the {@link ITestCaseData}.
	 * 
	 * @param obj    the {@link IProblemAndTestCaseData} object to write
	 * @param writer the Writer
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void writeProblemAndTestCaseData(
			IProblemAndTestCaseData<? extends IProblemData, ? extends ITestCaseData> obj,
			Writer writer) throws IOException {
		JSONArray array = new JSONArray();
		array.add(convertModelObjectToJSON(obj.getProblem(), IProblemData.SCHEMA));
		for (ITestCaseData testCase : obj.getTestCaseData()) {
			array.add(convertModelObjectToJSON(testCase, ITestCase.SCHEMA));
		}
		JSONValue.writeJSONString(array, writer);
	}

	/**
	 * Convert a model object to an object suitable for output via {@link JSONValue}.
	 * 
	 * @param obj     the model object
	 * @param schema  the model object's schema
	 * @return the object suitable for output as JSON
	 */
	public static<E> Object convertModelObjectToJSON(E obj, ModelObjectSchema<E> schema) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			Object value = field.get(obj);
			
			// Special case: convert enum values to int
			if (value instanceof Enum) {
				Enum<?> e = (Enum<?>) value;
				value = (Integer) e.ordinal();
			}
			
			result.put(field.getName(), value);
		}
		
		return result;
	}
	
	/**
	 * Read JSON-encoded data into a {@link IProblemAndTestCaseData} object.
	 * 
	 * @param obj              the {@link IProblemAndTestCaseData} object
	 * @param problemFactory   factory to create {@link IProblemData} objects
	 * @param testCaseFactory  factory to create {@link ITestCaseData} objects
	 * @param reader           the Reader to read fromXMLConversionTest
	 * @throws IOException
	 */
	public static<ProblemDataType extends IProblemData, TestCaseDataType extends ITestCaseData> void readProblemAndTestCaseData(
			IProblemAndTestCaseData<ProblemDataType, TestCaseDataType> obj,
			IFactory<ProblemDataType> problemFactory,
			IFactory<TestCaseDataType> testCaseFactory,
			Reader reader)
			throws IOException {
		JSONParser parser = new JSONParser();
		
		try {
			Object parsed = parser.parse(reader);
			if (!(parsed instanceof List)) {
				throw new IOException("Expecting JSON list");
			}
			
			List<?> elements = (List<?>) parsed;
			Iterator<?> i = elements.iterator();
			
			// First object is problem data, subsequent objects are test case data
			
			// Problem data
			ProblemDataType problem = problemFactory.create();
			convertJSONToModelObject(i.next(), problem, IProblemData.SCHEMA);
			obj.setProblem(problem);
			
			// Test case data
			while (i.hasNext()) {
				TestCaseDataType testCase = testCaseFactory.create();
				convertJSONToModelObject(i.next(), testCase, ITestCaseData.SCHEMA);
				obj.addTestCase(testCase);
			}
		} catch (ParseException e) {
			throw new IOException("Invalid JSON input", e);
		}
	}
	
	/**
	 * Convert a JSON value into model object data.
	 * 
	 * @param jsonObj    the JSON object
	 * @param modelObj   the model object to populate based on the JSON object data
	 * @param schema     the model object's schema
	 * @throws IOException
	 */
	public static<E> void convertJSONToModelObject(Object jsonObj, E modelObj, ModelObjectSchema<E> schema) throws IOException {
		if (!(jsonObj instanceof Map)) {
			throw new IOException("JSON value to convert to model object is not a map");
		}
		
		Map<?, ?> map = (Map<?,?>) jsonObj;
		
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			Object jsonFieldValue = map.get(field.getName());
			if (jsonFieldValue != null) {
				Object value = decodeJsonValue(jsonFieldValue, field.getType());
				field.setUntyped(modelObj, value);
			}
		}
	}

	private static Object decodeJsonValue(Object jsonFieldValue, Class<?> type) throws IOException {
		// Easy case: value is already the correct type
		if (jsonFieldValue.getClass() == type) {
			return jsonFieldValue;
		}
		
		// Integer encoding of enumeration value?
		if (type.isEnum()) {
			if (!(jsonFieldValue instanceof Number)) {
				throw new IOException("Cannot convert " + jsonFieldValue.getClass() + " to enumeration member");
			}
			int ordinal = ((Number)jsonFieldValue).intValue();
			Object[] members = type.getEnumConstants();
			if (ordinal < 0 || ordinal >= members.length) {
				throw new IOException("Invalid ordinal " + ordinal + " for enumeration " + type.getClass().getName());
			}
			return members[ordinal];
		}

		if (jsonFieldValue instanceof Number) {
			Number n = (Number) jsonFieldValue;
			// Numeric conversions
			if (type == Integer.class) {
				return n.intValue();
			} else if (type == Long.class) {
				return n.longValue();
			}
		}
		
		throw new IOException("Cannot convert " + jsonFieldValue.getClass().getName() + " to " + type.getName());
	}
}
