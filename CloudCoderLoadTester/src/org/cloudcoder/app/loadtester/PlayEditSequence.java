package org.cloudcoder.app.loadtester;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.QuizEndedException;

/**
 * Play an {@link EditSequence}.
 * 
 * @author David Hovemeyer
 */
public class PlayEditSequence {
	/**
	 * Default interval for sending batches of {@link Change}s
	 * to the webapp.
	 */
	public static final long SEND_BATCH_INTERVAL_MS = 2000L;
	
	private Client client;
	private String exerciseName;
	private EditSequence editSequence;
	private Problem problem;
	private long sendBatchIntervalMs;
	
	/**
	 * Constructor.
	 */
	public PlayEditSequence() {
		this.sendBatchIntervalMs = SEND_BATCH_INTERVAL_MS;
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
	 * Set the interval (in milliseconds) at which batches of {@link Change}s should
	 * be sent.  Defaults to {@link #SEND_BATCH_INTERVAL_MS}.
	 * 
	 * @param sendBatchIntervalMs interval at which batches of {@link Change}s should be
	 *                            sent, in milliseconds
	 */
	public void setSendBatchIntervalMs(long sendBatchIntervalMs) {
		this.sendBatchIntervalMs = sendBatchIntervalMs;
	}

	/**
	 * Prepare to play the {@link EditSequence}.
	 * This should be called once, before the first call to {@link #play()}.
	 * 
	 * @throws CloudCoderAuthenticationException
	 */
	public void setup() throws CloudCoderAuthenticationException {
		// Fix up all of the Change objects by resetting the event id
		// to the default (0) value and changing the user id to that of the
		// logged-in user.
		List<Change> changeList = editSequence.getChangeList();
		for (Change change : changeList) {
			change.getEvent().setId(0);
			change.setEventId(0);
			change.getEvent().setUserId(client.getUser().getId());
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
	 * Play the {@link EditSequence}, attempting to emulate the
	 * relative timing and batching of the original Change events.
	 * This should be called after calling {@link #setup()},
	 * and can be called any number of times.
	 * 
	 * @param onSend callback to invoke when Changes are sent
	 * @throws CloudCoderAuthenticationException
	 * @throws InterruptedException 
	 * @throws QuizEndedException 
	 */
	public void play(ICallback<Change[]> onSend) throws CloudCoderAuthenticationException, InterruptedException, QuizEndedException {
		boolean done = false;
		
		// Schedule the first "timer" event where a batch of Changes
		// can be sent
		long nextSend = System.currentTimeMillis() + sendBatchIntervalMs;
		
		// Use the timestamp of the first Change in the EditSequence
		// to set the initial window for sending Changes
		long windowStart = editSequence.getChangeList().get(0).getEvent().getTimestamp();
		
		while (!done) {
			// Get all Changes to be sent in the next batch.
			// This will update the 
			List<Change> batch = new ArrayList<Change>();
			windowStart = createBatch(windowStart, batch);

			if (windowStart < 0L) {
				// All Changes have been sent, so we're done
				done = true;
			} else {
				// Wait until next time to send.
				// We're basically emulating the repeating timer that the
				// webapp's client-side javascript code uses to flush
				// accumulated Changes periodically.
				long delay = nextSend - System.currentTimeMillis();
				if (delay > 0L) {
					Thread.sleep(delay);
				}

				// Schedule the next send time
				nextSend = System.currentTimeMillis() + sendBatchIntervalMs;

				// If there is a batch of changes to send, send them
				if (batch.size() > 0) {
					Change[] arr = batch.toArray(new Change[batch.size()]);
					onSend.call(arr);
					client.sendChanges(arr);
				}
			}
		}
	}

	/**
	 * Create a batch of {@link Change}s, containing all changes
	 * within {@link #sendBatchIntervalMs} of the given window start
	 * time.
	 * 
	 * @param windowStart the window start time
	 * @param batch       a list to which {@link Change}s in the current
	 *                    window should be added
	 * @return the start time for the next window, or -1L if there are no more
	 *         changes in the {@link EditSequence} to send
	 */
	private long createBatch(long windowStart, List<Change> batch) {
		// Assume that all changes have been sent
		boolean allChangesSent = true;
		
		// Find Changes that are within the current window to add
		// to the batch.  It is possible that there are none.
		// Also, check to see if we can find any Changes
		// with a timestamp greater than the current window start.
		// If there aren't any, then we know that all Changes
		// have been sent.
		for (Change c : editSequence.getChangeList()) {
			if (c.getEvent().getTimestamp() >= windowStart) {
				// There is at least one Change with a timestamp greater than
				// the window start timestamp, so we haven't yet sent all of
				// the Changes in the EditSequence
				allChangesSent = false;
				if (c.getEvent().getTimestamp() < windowStart + sendBatchIntervalMs) {
					// This Change is within the current window, so add it to
					// the batch
					batch.add(c);
				}
			}
		}
		
		// Return the start time for the next window,
		// or -1L if all of the Changes in the EditSequence have
		// been sent
		return allChangesSent ? -1L : windowStart + sendBatchIntervalMs;
	}
}
