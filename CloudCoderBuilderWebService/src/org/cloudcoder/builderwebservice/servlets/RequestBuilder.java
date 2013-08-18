package org.cloudcoder.builderwebservice.servlets;

import static org.cloudcoder.app.shared.model.json.JSONUtil.*;

import java.util.Map;

import org.cloudcoder.webservice.util.BadRequestException;
import org.json.simple.JSONArray;

public class RequestBuilder {
	private Object requestObj_;
	
	public RequestBuilder(Object requestObj) {
		this.requestObj_ = requestObj;
	}
	
	public Request build() throws BadRequestException {
		Map<?, ?> requestObj = expectObject(requestObj_);
		
		// The Data field should contain the code execution request
		Map<?, ?> data = expectObject(requiredField(requestObj, "Data"));
		
		// Extract field values
		Request request = new Request();
		
		try {
			request.setLanguage(expect(String.class, requiredField(data, "Language")));
			request.setExecutionType(expectInteger(requiredField(data, "ExecutionType")));
			request.setTestname(expect(String.class, requiredField(data, "Testname")));
			request.setCodeArray(expect(JSONArray.class, requiredField(data, "Code")));
			request.setTestcaseType(expectInteger(requiredField(data, "TestcaseType")));
			request.setTrace(expect(Boolean.class, requiredField(data, "Trace")));
			request.setStdout(expect(Boolean.class, requiredField(data, "Stdout")));
			request.setReturnValue(expect(Boolean.class, requiredField(data, "ReturnValue")));
			request.setTestcases(expect(JSONArray.class, requiredField(data, "Testcases")));
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Invalid JSON request", e);
		}
		
		return request;
	}
}
