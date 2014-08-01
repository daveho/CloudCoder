package org.cloudcoder.dataanalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.EventType;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.cloudcoder.app.shared.model.WorkSession;

public class PauseTimes implements IAnalyzeSnapshots {
	private SnapshotSelectionCriteria criteria;
	private int separation;
	private String outputFile;

	@Override
	public void setConfig(Properties config) {
	}
	
	@Override
	public void setCriteria(SnapshotSelectionCriteria criteria) {
		this.criteria = criteria;
	}
	
	public void setSeparation(int separation) {
		this.separation = separation;
	}
	
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	private void execute() throws IOException {
		BufferedWriter w = null;
		
		try {
			FileWriter fw = new FileWriter(outputFile);
			w = new BufferedWriter(fw);
			
			w.write("userId,pauseTime\n");
			
			List<WorkSession> sessions = Database.getInstance().findWorkSessions(criteria, separation);
			System.out.println("Found " + sessions.size() + " work sessions");
			
			int count = 0;
			
			for (WorkSession ws : sessions) {
				List<Pair<Event, Change>> events =
						Database.getInstance().getEventsWithChanges(ws.getUserId(), ws.getProblemId(), ws.getStartEventId(), ws.getEndEventId());
				
				long last = -1;
				for (Pair<Event, Change> pair : events) {
					// ignore full-text change events: they are generated automatically
					// with each submission
					if (pair.getLeft().getType() == EventType.CHANGE && pair.getRight().getType() == ChangeType.FULL_TEXT) {
						continue;
					}
					if (last > 0L) {
						w.write(ws.getUserId() + "," + (pair.getLeft().getTimestamp() - last) + "\n");
						
						count++;
						if (count % 200 == 0) {
							System.out.print(".");
							System.out.flush();
						}
					}
					last = pair.getLeft().getTimestamp();
				}
			}
			w.flush();
			System.out.println("done");
		} finally {
			IOUtils.closeQuietly(w);
		}
	}
	
	public static void main(String[] args) throws IOException {
		Scanner keyboard = new Scanner(System.in);
		PauseTimes p = new PauseTimes();
		
		Util.configureCriteriaAndDatabase(keyboard, p, args);
		
		int separation = Integer.parseInt(Util.ask(keyboard, "Work session separation in seconds: "));
		p.setSeparation(separation);
		String outputFile = Util.ask(keyboard, "Output file: ");
		p.setOutputFile(outputFile);
		
		p.execute();
	}
}
