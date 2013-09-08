package org.cloudcoder.builderwebservice.servlets;

import org.json.simple.JSONArray;

/**
 * Code execution request submitted to the builder web service.
 * This is a thin wrapper over the submitted JSON data.
 * 
 * @author David Hovemeyer
 */
public class Request {
	private String language;
	private Integer executionType;
	private String testname;
	private JSONArray codeArray;
	private Integer testcaseType;
	private Boolean trace;
	private Boolean stdout;
	private Boolean returnValue;
	private JSONArray testcases;
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setExecutionType(Integer executionType) {
		this.executionType = executionType;
	}
	
	public void setTestname(String testname) {
		this.testname = testname;
	}
	
	public String getTestname() {
		return testname;
	}
	
	public Integer getExecutionType() {
		return executionType;
	}
	
	public void setCodeArray(JSONArray codeArray) {
		this.codeArray = codeArray;
	}
	
	public JSONArray getCodeArray() {
		return codeArray;
	}
	
	public void setTestcaseType(Integer testcaseType) {
		this.testcaseType = testcaseType;
	}
	
	public Integer getTestcaseType() {
		return testcaseType;
	}
	
	public void setTrace(Boolean trace) {
		this.trace = trace;
	}
	
	public Boolean getTrace() {
		return trace;
	}
	
	public void setStdout(Boolean stdout) {
		this.stdout = stdout;
	}
	
	public Boolean getStdout() {
		return stdout;
	}
	
	public void setReturnValue(Boolean returnValue) {
		this.returnValue = returnValue;
	}
	
	public Boolean getReturnValue() {
		return returnValue;
	}
	
	public void setTestcases(JSONArray testcases) {
		this.testcases = testcases;
	}
	
	public JSONArray getTestcases() {
		return testcases;
	}

}
