package org.cloudcoder.progsnap2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.dataanalysis.Util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class MergeCompileErrors {

	public void execute(String[] args) throws FileNotFoundException, IOException {
		Scanner keyboard = new Scanner(System.in);
		Properties config = Export.getExportConfig(args, keyboard);

		Util.connectToDatabase(config);
		
		String baseDir = config.getProperty("ps2.dest");
		
		File updatedMainEventTable = new File(baseDir + "/UpdatedMainTable.csv");
		if (updatedMainEventTable.exists()) {
			throw new IllegalStateException(updatedMainEventTable.getAbsolutePath() + " already exists!");
		}
		
		try (InputStream is = new FileInputStream(baseDir + "/MainTable.csv")) {
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			@SuppressWarnings("resource")
			CSVReader r = new CSVReader(isr);
			
			// Read the header row
			Header origHeader = new Header();
			origHeader.init(r.readNext());
			
			try (OutputStream os = new FileOutputStream(updatedMainEventTable)) {
				OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
				@SuppressWarnings("resource")
				CSVWriter w = new CSVWriter(osw);
				
				// Create Header for updated main event table
				Header updatedHeader = origHeader.extend("SourceLocation", "CompileMessageType", "CompileMessageData");
				
				// Write header row for updated main event table
				w.writeNext(updatedHeader.getHeaderRow());
				
				for (;;) {
					String[] origRow = r.readNext();
					if (origRow == null) {
						break;
					}
					RowView origView = origHeader.asRowView(origRow);
					
					// Copy the original record, extending it with the additional columns
					// (which will be empty)
					String[] updatedRow = updatedHeader.createRow();
					updatedHeader.copyValues(origRow, updatedRow);
					w.writeNext(updatedRow);
					
					// See if this event is a Compile event
					String eventType = origView.get("EventType");
					
					if (eventType.equals("Compile")) {
						String eventId = origHeader.getValue("EventID", origRow);
						long eventIdAsLong = Long.parseLong(eventId);
						File diagEvents = new File(baseDir + "/diag/" + eventId + ".csv");
						System.out.printf("  Diagnostics '%s' -> %s\n", diagEvents.getPath(), diagEvents.exists() ? "yes" : "no");
						if (diagEvents.exists()) {
							// We have compiler diagnostics for this Compile event!
							// Merge them in.
							
							long orderAsLong = Long.parseLong(origView.get("Order"));

							// Read diagnostics
							try (InputStream diagIn = new FileInputStream(diagEvents)) {
								InputStreamReader diagRdr = new InputStreamReader(diagIn, StandardCharsets.UTF_8);
								CSVReader dr = new CSVReader(diagRdr);
								Header diagHeader = new Header();
								diagHeader.init(dr.readNext());
								
								// Event IDs and Order values for compiler diagnostics are generated in
								// the gap created by the spacing that the Export class uses for generating
								// the EventID and Order values in the main event table.
								long diagEventId = eventIdAsLong + 49L;
								long diagOrder = orderAsLong + 50L;
								
								for (;;) {
									String[] diagRow = dr.readNext();
									if (diagRow == null) {
										break;
									}
									RowView diagRowView = diagHeader.asRowView(diagRow);
									
									String[] outRow = updatedHeader.createRow();
									RowView outRowView = updatedHeader.asRowView(outRow);
									
									// Populate data values
									outRowView.put("EventID", String.valueOf(diagEventId++));
									outRowView.put("Order", String.valueOf(diagOrder++));
									String[] columnsToCopy = {
											"EventType", "SubjectID", "ToolInstances", "CodeStateID",
											"ServerTimestamp", "ServerTimezone", "CourseID", "ProblemID",
									};
									for (String colName : columnsToCopy) {
										outRowView.copyFrom(origView, colName);
									}
									outRowView.copyFrom(diagRowView, "EventType");
									outRowView.copyFrom(diagRowView, "ParentEventID");
									outRowView.copyFrom(diagRowView, "CodeStateSection");
									outRowView.copyFrom(diagRowView, "SourceLocation");
									outRowView.copyFrom(diagRowView, "CompileMessageType");
									outRowView.copyFrom(diagRowView, "CompileMessageData");
									
									// Write the diagnostic event to the updated main event table
									w.writeNext(outRow);
								}
							}
						}
					}
				}
				w.flush();
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		MergeCompileErrors mce = new MergeCompileErrors();
		mce.execute(args);
	}
}
