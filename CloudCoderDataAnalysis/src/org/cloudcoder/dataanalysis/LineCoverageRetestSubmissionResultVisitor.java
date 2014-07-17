package org.cloudcoder.dataanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.shared.model.LineCoverage;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionResultAnnotation;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVWriter;

public class LineCoverageRetestSubmissionResultVisitor implements IRetestSubmissionResultVisitor {
	private static final Logger logger = LoggerFactory.getLogger(LineCoverageRetestSubmissionResultVisitor.class);
	
	private BufferedWriter writer;
	private CSVWriter csvWriter;
	
	@Override
	public void init(File outputDirectory) {
		try {
			writer = new BufferedWriter(new FileWriter(new File(outputDirectory, "coverage.csv")));
			//csvWriter = 
			CSV csv = CSV
					.separator('|')  // delimiter of fields
					.quote('"')      // quote character
					.create();       // new instance is immutable
			csvWriter = csv.writer(writer);

			// We put a heck of a lot of information in each CSV record
			csvWriter.writeNext(
				"submitEventId",
				"fullTextChangeId",
				"courseId",
				"problemId",
				"userId",
				"compilationOutcome",
				"aggregateCoveragePercent",
				"aggregateCoverage",
				"individualCoveragePercent(multi)",
				"individualCoverage(multi)"
			);
		} catch (IOException e) {
			logger.error("Couldn't open coverage results data file", e);
		}
	}

	@Override
	public void onSubmissionResult(SubmissionResult result, RetestSnapshot snapshot) {
		if (writer == null) {
			return;
		}
		
		LineCoverage aggregate = null;
		List<LineCoverage> individual = null;
		String aggregateAsString = null;
		for (SubmissionResultAnnotation annotation : result.getAnnotationList()) {
			String value = annotation.getValue();
			if (annotation.getKey().equals("LineCoverageAggregate")) {
				aggregate = deserializeJson(value);
				aggregateAsString = value;
			} else if (annotation.getKey().equals("LineCoverage")) {
				individual = deserializeJSONArray(value);
			}
		}
		
		List<Object> record = new ArrayList<Object>();

		record.addAll(Arrays.asList(
				snapshot.submitEventId,
				snapshot.fullTextChangeId,
				snapshot.courseId,
				snapshot.problemId,
				snapshot.userId,
				result.getCompilationResult().getOutcome().ordinal()
		));

		if (aggregate != null) {
			record.add(aggregate.getPercent());
		}

		if (aggregateAsString != null) {
			record.add(aggregateAsString);
		}

		if (individual != null) {
			for (LineCoverage l : individual) {
				record.add(l.getPercent());
			}

			for (LineCoverage l : individual) {
				// The exceptions here shouldn't happen because the LineCoverage
				// objects in the individual list were parsed from JSON, so converting
				// them back to JSON shouldn't fail.
				try {
					record.add(JSONConversion.genericConvertPojoToString(l));
				} catch (JsonGenerationException e) {
					logger.error("Shouldn't happen", e);
				} catch (JsonMappingException e) {
					logger.error("Shouldn't happen", e);
				}
			}
		}

		// Write coverage data for this submission result / snapshot
		if (!record.isEmpty()) {
			writeAll(record);
		}
	}

	private LineCoverage deserializeJson(String value) {
		LineCoverage coverage = null;
		try {
			coverage = JSONConversion.genericConvertPojoFromString(value, LineCoverage.class);
		} catch (JsonParseException e) {
			logger.error("Error converting JSON to LineCoverage", e);
		} catch (JsonMappingException e) {
			logger.error("Error converting JSON to LineCoverage", e);
		}
		return coverage;
	}
	
	private List<LineCoverage> deserializeJSONArray(String value) {
		LineCoverage[] coverageList = null;
		try {
			coverageList = JSONConversion.genericConvertPojoFromString(value, LineCoverage[].class);
		} catch (JsonParseException e) {
			logger.error("Error converting JSON to LineCoverage array", e);
		} catch (JsonMappingException e) {
			logger.error("Error converting JSON to LineCoverage array", e);
		}
		return coverageList != null ? Arrays.asList(coverageList) : null;
	}

	@Override
	public void cleanup() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("Error closing coverage results data file", e);
		}
	}

	private void writeAll(List<Object> values) {
		String[] sarr = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			sarr[i] = values.get(i).toString();
		}
		csvWriter.writeNext(sarr);
	}
}
