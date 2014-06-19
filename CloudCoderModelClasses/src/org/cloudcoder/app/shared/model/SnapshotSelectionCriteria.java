package org.cloudcoder.app.shared.model;

public class SnapshotSelectionCriteria {
	/**
	 * Select any course/problem/user.
	 */
	public static final int ANY = -1;
	
	private int courseId;
	private int problemId;
	private int userId;
	
	public SnapshotSelectionCriteria() {
		courseId = ANY;
		problemId = ANY;
		userId = ANY;
	}

	/**
	 * Set the course id to select.
	 * Use {@link #ANY} to select all courses.
	 * 
	 * @param courseId the course id to select
	 */
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}

	/**
	 * @return the course id to select, {@link #ANY} if all courses will be selected
	 */
	public int getCourseId() {
		return courseId;
	}
	
	/**
	 * Set the problem id to select.
	 * Use {@link #ANY} to select all problems.
	 * 
	 * @param problemId the problem id to select
	 */
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	/**
	 * @return the problem id to select, {@link #ANY} if all problems will be selected
	 */
	public int getProblemId() {
		return problemId;
	}
	
	/**
	 * Set the user id to select.
	 * Use {@link #ANY} to select all users.
	 * 
	 * @param userId the user id to select
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * @return the user id to select, {@link #ANY} if all users will be selected
	 */
	public int getUserId() {
		return userId;
	}
}
