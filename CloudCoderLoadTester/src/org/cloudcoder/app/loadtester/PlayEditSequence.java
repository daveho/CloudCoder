package org.cloudcoder.app.loadtester;

import java.util.List;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.Problem;

/**
 * Play an {@link EditSequence}.
 * 
 * @author David Hovemeyer
 */
public class PlayEditSequence {
	private Client client;
	private String exerciseName;
	private EditSequence editSequence;
	private Problem problem;
	
	/**
	 * Constructor.
	 */
	public PlayEditSequence() {
	}
	
	/**
	 * Set the {@link Client} to use to communicate with the webapp.
	 * It should already be logged in.
	 * 
	 * @param client the {@link Client}
	 */
	public void setClient(Client client) {
		this.client = client;
	}
	
	/**
	 * Set the name of the exercise.
	 * 
	 * @param exerciseName the name of the exercise
	 */
	public void setExerciseName(String exerciseName) {
		this.exerciseName = exerciseName;
	}

	/**
	 * Set the {@link EditSequence} to play.
	 * 
	 * @param editSequence the {@link EditSequence}
	 */
	public void setEditSequence(EditSequence editSequence) {
		this.editSequence = editSequence;
	}

	/**
	 * Prepare to play the {@link EditSequence}.
	 * This should be called once, before the first call to {@link #play()}.
	 * 
	 * @throws CloudCoderAuthenticationException
	 */
	public void setup() throws CloudCoderAuthenticationException {
		// Fix up all of the Change objects.
		// The Event can be removed, and the event id can be set to
		// a default value.  (Might not be strictly necessary,
		// but matches the behavior of the actual webapp client
		// javascript code.)
		List<Change> changeList = editSequence.getChangeList();
		for (Change change : changeList) {
			change.setEvent(null);
			change.setEventId(0);
		}
		
		// Find the Problem (exercise)
		CourseAndCourseRegistration[] courses = client.getRegisteredCourses();
		for (CourseAndCourseRegistration c : courses) {
			Problem[] problems = client.getProblemsForCourse(c.getCourse());
			for (Problem p : problems) {
				if (p.getTestname().equals(exerciseName)) {
					this.problem = p;
				}
			}
		}
		if (this.problem == null) {
			throw new RuntimeException("Could not find exercise " + exerciseName);
		}
		
		// Set the Problem
		client.setProblem(problem);
	}
	
	/**
	 * Play the {@link EditSequence}.
	 * This should be called after calling {@link #setup()},
	 * and can be called any number of times.
	 * 
	 * @throws CloudCoderAuthenticationException
	 */
	public void play() throws CloudCoderAuthenticationException {
	}
}
