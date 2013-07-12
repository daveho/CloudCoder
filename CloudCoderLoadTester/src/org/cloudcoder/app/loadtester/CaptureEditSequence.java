package org.cloudcoder.app.loadtester;

import java.util.List;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.shared.model.Change;

/**
 * Capture a series of edits ({@link Change}s) that were saved to the database.
 * For a given user and problem (exercise), all change events in a specified
 * range of event ids will be captured.
 * 
 * @author David Hovemeyer
 */
public class CaptureEditSequence {
	private int userId;
	private int problemId;
	private int minEventId;
	private int maxEventId;
	private List<Change> changeList;
	
	/**
	 * Constructor.
	 * Setters must be called before the object is used.
	 */
	public CaptureEditSequence() {
		
	}
	
	/**
	 * Set the user id.
	 * 
	 * @param userId the user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Set the problem id.
	 * 
	 * @param problemId the problem id
	 */
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	/**
	 * Set the minimum event id.
	 * 
	 * @param minEventId the minimum event id
	 */
	public void setMinEventId(int minEventId) {
		this.minEventId = minEventId;
	}
	
	/**
	 * Set the maximum event id.
	 * 
	 * @param maxEventId the maximum event id
	 */
	public void setMaxEventId(int maxEventId) {
		this.maxEventId = maxEventId;
	}
	
	public void capture() {
		IDatabase db = Database.getInstance();
		List<Change> captured = db.loadChanges(userId, problemId, minEventId, maxEventId);
		changeList.addAll(captured);
	}
}
