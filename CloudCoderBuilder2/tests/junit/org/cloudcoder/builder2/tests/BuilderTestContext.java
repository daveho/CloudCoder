package org.cloudcoder.builder2.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import org.cloudcoder.app.shared.model.IFunction;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;
import org.cloudcoder.builder2.server.Builder2;
import org.cloudcoder.builder2.server.Global;
import org.cloudcoder.daemon.IOUtil;

public class BuilderTestContext {
	
	private Properties config;
	private Builder2 builder;
	
	public BuilderTestContext() {
		config = new Properties();
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.enable", "true");
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.heapsize", "8388608");
		config.setProperty("cloudcoder.builder2.tmpdir", System.getProperty("java.io.tmpdir"));
		
		builder = new Builder2(config);
	}
	
	public ProblemAndTestCaseList getExercise(String name) {
		return invokeOnResource(name + ".json", new IFunction<Reader, ProblemAndTestCaseList>() {
			@Override
			public ProblemAndTestCaseList invoke(Reader arg) {
				try {
					ProblemAndTestCaseList exercise = new ProblemAndTestCaseList();
					JSONConversion.readProblemAndTestCaseData(
							exercise,
							ReflectionFactory.forClass(Problem.class),
							ReflectionFactory.forClass(TestCase.class),
							arg);
					return exercise;
				} catch (IOException e) {
					throw new IllegalStateException("Could not read exercise", e);
				}
			}
		});
	}

	public String getSourceText(final String resourceName) {
		return invokeOnResource(resourceName, new IFunction<Reader, String>() {
			@Override
			public String invoke(Reader arg) {
				StringWriter sw = new StringWriter();
				try {
					IOUtil.copy(arg, sw);
				} catch (IOException e) {
					throw new IllegalStateException("Error reading source " + resourceName);
				}
				return sw.toString();
			}
		});
	}

	/**
	 * Invoke an {@link IFunction} on given resource.
	 * The function will receive a {@link Reader} as its parameter.
	 * 
	 * @param resourceName the name of the resource
	 * @param f            the function to invoke
	 * @return the result of the function
	 */
	private<E> E invokeOnResource(String resourceName, IFunction<Reader, E> f) {
		InputStream in = getClass().getClassLoader().getResourceAsStream("org/cloudcoder/builder2/tests/res/" + resourceName);
		if (in == null) {
			throw new IllegalStateException("Could not load resource: " + resourceName);
		}
		Reader r = new InputStreamReader(in, Charset.forName("UTF-8"));
		try {
			return f.invoke(r);
		} finally {
			IOUtil.closeQuietly(r);
		}
	}
	
	public void setup() {
		Global.setup(config);
	}

	public void cleanup() {
		Global.cleanup(config);
	}

	public SubmissionResult testSubmission(String source, ProblemAndTestCaseList exercise) {
		return builder.testSubmission(exercise.getProblem(), exercise.getTestCaseData(), source);
	}
}
