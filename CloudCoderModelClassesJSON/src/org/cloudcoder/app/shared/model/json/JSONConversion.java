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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.IProblemAndTestCaseData;
import org.cloudcoder.app.shared.model.IProblemData;
import org.cloudcoder.app.shared.model.ITestCaseData;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
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
	 * Write an {@link IProblemAndTestCaseData} object.
	 * 
	 * @param obj    the {@link IProblemAndTestCaseData} object to write
	 * @param writer the Writer
	 * @throws IOException
	 */
	public static void writeProblemAndTestCaseData(
			IProblemAndTestCaseData<? extends IProblemData, ? extends ITestCaseData> obj,
			Writer writer) throws IOException {
		
		LinkedHashMap<String, Object> result = convertProblemAndTestCaseDataToJSONObject(obj);
		
		JSONValue.writeJSONString(result, writer);
	}

	/**
	 * Convert an {@link IProblemAndTestCaseData} object to a JSON object
	 * (suitable for being encoded as a JSON string.)
	 * 
	 * @param obj the {@link IProblemAndTestCaseData} object
	 * @return the encoded JSON object
	 */
	public static LinkedHashMap<String, Object> convertProblemAndTestCaseDataToJSONObject(
			IProblemAndTestCaseData<? extends IProblemData, ? extends ITestCaseData> obj) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		// Add the problem
		result.put(IProblemData.SCHEMA.getName(), convertModelObjectToJSON(obj.getProblem(), IProblemData.SCHEMA));

		// Add array of test cases
		addModelObjectList(result, obj.getTestCaseData(), ITestCaseData.SCHEMA);
		return result;
	}

	/**
	 * Convert a {@link RepoProblemSearchResult} to a JSON object.
	 * 
	 * @param searchResult a {@link RepoProblemSearchResult}
	 * @return JSON object
	 */
	@SuppressWarnings("unchecked")
	public static Object convertRepoProblemSearchResultToJSON(RepoProblemSearchResult searchResult) {
		LinkedHashMap<String, Object> jsonObj = new LinkedHashMap<String, Object>();
		
		jsonObj.put(RepoProblem.SCHEMA.getName(), JSONConversion.convertModelObjectToJSON(searchResult.getRepoProblem(), RepoProblem.SCHEMA));
		
		JSONArray tagList = new JSONArray();
		for (String tag : searchResult.getMatchedTagList()) {
			tagList.add(tag);
		}
		jsonObj.put(RepoProblemSearchResult.MATCHED_TAG_LIST_ELEMENT_NAME, tagList);
		
		return jsonObj;
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
	 * Convert a list of model objects to a JSON array object.
	 * 
	 * @param objList list of model objects
	 * @param schema  the schema for the model objects
	 * @return a JSON array object
	 */
	@SuppressWarnings("unchecked")
	public static<E> Object convertModelObjectListToJSON(List<? extends E> objList, ModelObjectSchema<E> schema) {
		JSONArray result = new JSONArray();
		
		for (E obj : objList) {
			result.add(convertModelObjectToJSON(obj, schema));
		}
		
		return result;
	}

	/**
	 * Add a list of model objects as a field of given JSON object.
	 * 
	 * @param jsonObj      the JSON object to add the list to
	 * @param modelObjList list of model objects
	 * @param schema       the schema for the list of model objects
	 */
	public static<E> void addModelObjectList(
			Map<String, Object> jsonObj,
			List<? extends E> modelObjList,
			ModelObjectSchema<E> schema) {
		jsonObj.put(schema.getListElementName(), convertModelObjectListToJSON(modelObjList, schema));
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

			// Convert the problem
			ProblemDataType problem = problemFactory.create();
			if (!convertJSONFieldToModelObject(parsed, problem, IProblemData.SCHEMA)) {
				throw new IOException("No problem data!");
			}
			obj.setProblem(problem);
			
			// Convert the test cases
			List<TestCaseDataType> testCaseList = new ArrayList<TestCaseDataType>();
			if (!convertJSONFieldToModelObjectList(parsed, testCaseList, testCaseFactory, ITestCaseData.SCHEMA)) {
				throw new IOException("No test case list!");
			}
			for (TestCaseDataType testCase : testCaseList) {
				obj.addTestCase(testCase);
			}
		} catch (ParseException e) {
			throw new IOException("Invalid JSON input", e);
		}
	}
	
	/**
	 * Convert a field of a JSON object to a model object.
	 * 
	 * @param jsonObj_  the JSON object
	 * @param modelObj  the model object to populate from the JSON object's field
	 * @param schema    the model object's schema
	 * @return true if the conversion was successful, false if there was no such field
	 * @throws IOException
	 */
	public static<E> boolean convertJSONFieldToModelObject(Object jsonObj_, E modelObj, ModelObjectSchema<E> schema)
			throws IOException {
		if (!(jsonObj_ instanceof Map)) {
			throw new IOException("Expecting JSON object");
		}
		Map<?,?> jsonObj = (Map<?,?>) jsonObj_;
		
		Object value = jsonObj.get(schema.getName());
		if (value == null) {
			// No such field
			return false;
		}
		
		convertJSONToModelObject(value, modelObj, schema);
		return true;
	}
	
	/**
	 * Convert a field of a JSON object to a list of model objects.
	 * The value of the JSON object's field must be an array.
	 * 
	 * @param jsonObj_         the JSON object
	 * @param modelObjList     the list to which the converted model objects should be added
	 * @param modelObjFactory  factory for creating model objects
	 * @param schema           the model objects' schema
	 * @return true if the conversion succeeds, false if there is no such array of model objects
	 *              in the JSON object
	 * @throws IOException
	 */
	public static<E> boolean convertJSONFieldToModelObjectList(
			Object jsonObj_,
			List<E> modelObjList,
			IFactory<E> modelObjFactory,
			ModelObjectSchema<? super E> schema) throws IOException {
		if (!(jsonObj_ instanceof Map)) {
			throw new IOException("Expecting JSON object");
		}
		Map<?,?> jsonObj = (Map<?,?>) jsonObj_;
		
		Object value = jsonObj.get(schema.getListElementName());
		if (value == null) {
			// No such field
			return false;
		}
		
		if (!(value instanceof JSONArray)) {
			throw new IOException("Expecting JSON array for value of " + schema.getListElementName() + " field");
		}
		
		JSONArray array = (JSONArray) value;
		Iterator<?> i = array.iterator();
		while (i.hasNext()) {
			E modelObj = modelObjFactory.create();
			convertJSONToModelObject(i.next(), modelObj, schema);
			modelObjList.add(modelObj);
		}
		
		return true;
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

	/**
	 * Decode a scalar JSON value.
	 * 
	 * @param jsonFieldValue a scalar JSON value
	 * @param type           the Java type to convert the JSON value to
	 * @return the Java value
	 * @throws IOException
	 */
	public static<E> E decodeJsonValue(Object jsonFieldValue, Class<E> type) throws IOException {
		// Easy case: value is already the correct type
		if (jsonFieldValue.getClass() == type) {
			return type.cast(jsonFieldValue);
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
			return type.cast(members[ordinal]);
		}

		// Numeric conversion required?
		if (jsonFieldValue instanceof Number) {
			Number n = (Number) jsonFieldValue;
			// Numeric conversions
			if (type == Integer.class) {
				return type.cast(n.intValue());
			} else if (type == Long.class) {
				return type.cast(n.longValue());
			}
		}
		
		// String encoding of numeric value?
		if (type == Integer.class && jsonFieldValue.getClass() == String.class) {
			return type.cast(Integer.parseInt((String) jsonFieldValue));
		}
		
		throw new IOException("Cannot convert " + jsonFieldValue.getClass().getName() + " to " + type.getName());
	}
}
