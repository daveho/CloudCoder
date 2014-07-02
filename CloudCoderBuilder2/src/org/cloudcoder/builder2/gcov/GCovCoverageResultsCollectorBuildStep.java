// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.gcov;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.LineCoverage;
import org.cloudcoder.app.shared.model.LineCoverageAggregator;
import org.cloudcoder.app.shared.model.LineCoverageRecord;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionResultAnnotation;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.builder2.ccompiler.Compiler;
import org.cloudcoder.builder2.ccompiler.Compiler.Module;
import org.cloudcoder.builder2.gcov.GCovFileParser.LineDataCallback;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.ISubmissionResultHook;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.NativeExecutable;
import org.cloudcoder.builder2.model.ProcessStatus;
import org.cloudcoder.builder2.process.ProcessRunner;
import org.cloudcoder.builder2.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Build step to collect coverage results and add them
 * as an annotation to the {@link SubmissionResult}.
 * This step is a no-op if gcov is not enabled
 * in cloudcoder.properties.
 * 
 * @author David Hovemeyer
 */
public class GCovCoverageResultsCollectorBuildStep implements IBuildStep {
	private static final Logger logger = LoggerFactory.getLogger(GCovCoverageResultsCollectorBuildStep.class);
	
	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		if (!PropertyUtil.isEnabled(config, "cloudcoder.builder2.cprog.gcov")) {
			return;
		}
		
		logger.debug("Collecting coverage results for submission...");
		
		Compiler compiler = submission.requireArtifact(this.getClass(), Compiler.class); 
		
		NativeExecutable nativeExe = submission.requireArtifact(this.getClass(), NativeExecutable.class);
		Command[] commandList = submission.requireArtifact(this.getClass(), Command[].class);
		
		File compileDir = nativeExe.getDir();

		// Get the source Module for the compilation (so we know what the source filename is) 
		Module module = compiler.getModules().get(0);

		// Run the gcov command for each command / test case
		// and collect the results as LineCoverage objects
		List<LineCoverage> allResults = new ArrayList<LineCoverage>();
		int testCaseCount = 0;
		for (Command command : commandList) {
			LineCoverage resultsForTestCase = collectCoverageResults(command, compileDir, module, config, testCaseCount++);
			allResults.add(resultsForTestCase);
		}
		
		// Aggregate line coverage
		LineCoverageAggregator aggregator = new LineCoverageAggregator();
		for (LineCoverage resultsForTestCase : allResults) {
			aggregator.process(resultsForTestCase);
		}
		LineCoverage aggregateResults = aggregator.getAggregate();
		
		// Convert LineCoverage results (both per-test-case and aggregate) to JSON
		try {
			final String jsonLineCoverageResults =
					JSONConversion.genericConvertPojoToString(allResults);
			final String jsonAggregateLineCoverageResults =
					JSONConversion.genericConvertPojoToString(aggregateResults);
			
			// Use a submission result hook to add the coverage results
			// as an annotation
			submission.addSubmissionResultHook(new ISubmissionResultHook() {
				@Override
				public void invoke(SubmissionResult result) {
					logger.info("Annotating SubmissionResult with LineCoverage...");
					SubmissionResultAnnotation annotation = new SubmissionResultAnnotation();
					annotation.setKey("LineCoverage");
					annotation.setValue(jsonLineCoverageResults);
					result.addAnnotation(annotation);
					SubmissionResultAnnotation annotation2 = new SubmissionResultAnnotation();
					annotation2.setKey("LineCoverageAggregate");
					annotation2.setValue(jsonAggregateLineCoverageResults);
					result.addAnnotation(annotation2);
				}
			});
		} catch (JsonGenerationException e) {
			logger.error("Could not convert coverage results to JSON", e);
		} catch (JsonMappingException e) {
			logger.error("Could not convert coverage results to JSON", e);
		}
	}

	private LineCoverage collectCoverageResults(
			Command command,
			File compileDir,
			Module module,
			Properties config,
			final int testCaseNumber) {
		String covDataDirName = command.getEnv().get("GCOV_PREFIX");
		File covDataDir = new File(compileDir, covDataDirName);
		
		// gcov is not my favorite program.
		// As of gcc-4.8:
		//   - If the -o option is used to make it look in a directory for
		//     coverage data, the .gcno file must be in that directory,
		//     meaning we have to copy it there
		//   - There is NO way to specify an output file name:
		//     the .gcov output file is created in whatever directory
		//     we run gcov in
		// For these reasons the easiest approach seems to be to copy
		// both the .gcno and source file into the coverage data directory,
		// and then run gcov from that directory.  This will produce a .c.gcov
		// file in that coverage data directory containing the coverage for
		// that specific test case.
		try {
			// Get all of the required files into the per-testcase directory
			// for this testcase.
			FileUtils.copyFile(new File(compileDir, module.sourceFileName), new File(covDataDir, module.sourceFileName));
			String gcnoFileName = FilenameUtils.getBaseName(module.sourceFileName) + ".gcno";
			FileUtils.copyFile(new File(compileDir, gcnoFileName), new File(covDataDir, gcnoFileName));
			
			// Run gcov, collect the coverage data
			ProcessRunner gcovRunner = new ProcessRunner(config);
			gcovRunner.runSynchronous(covDataDir, "gcov", module.sourceFileName);
			String gcovFileName = module.sourceFileName + ".gcov";
			File gcovFile = new File(covDataDir, gcovFileName);

			final LineCoverage results = new LineCoverage();
			results.setTestCaseNumber(testCaseNumber);

			if (gcovRunner.getStatus() != ProcessStatus.EXITED) {
				logger.error("gcov process failed with status={}", gcovRunner.getStatus());
			} else if (gcovRunner.getExitCode() != 0) {
				logger.error("gcov exited with exit code={}", gcovRunner.getExitCode());
			} else if (!gcovFile.exists()) {
				logger.error("gcov failed to produce expected output file {}", gcovFile.getPath());
			} else {
				// Use a GCovFileParser to populate the LineCoverage object
				FileReader r = new FileReader(gcovFile);
				try {
					GCovFileParser parser = new GCovFileParser(r);
					parser.parse(new LineDataCallback() {
						@Override
						public void onLineData(int lineNumber, int timesExecuted) {
							//System.out.printf("gcov: line=%d, timesExecuted=%d\n", lineNumber, timesExecuted);
							results.addRecord(new LineCoverageRecord(lineNumber, timesExecuted));
						}
					});
				} finally {
					IOUtils.closeQuietly(r);
				}
			}
			
			return results;
		} catch (IOException e) {
			throw new InternalBuilderException("Error collecting coverage data", e);
		}
	}
}
