package org.cloudcoder.repoapp.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;

/**
 * Common base class for servlets whose GET methods
 * access an exercise by hash code.
 * 
 * @author David Hovemeyer
 */
public abstract class LoadExerciseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public LoadExerciseServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			ServletUtil.badRequest(resp, "Missing hash");
			return;
		}

		// get rid of leading "/" from the hash
		String hash = pathInfo.substring(1);

		// Load the exercise
		RepoProblemAndTestCaseList exercise = Database.getInstance().getRepoProblemAndTestCaseList(hash);
		if (exercise == null) {
			ServletUtil.notFound(resp, "No exercise with hash " + hash);
			return;
		}

		doExercise(req, resp, exercise);
	}

	/**
	 * Downcall method to do something with an exercise accessed
	 * by the GET method.  Should generate a response.
	 * 
	 * @param req      the {@link HttpServletRequest}
	 * @param resp     the {@link HttpServletResponse} to generate
	 * @param exercise the exercise
	 * @throws ServletException
	 * @throws IOException
	 */
	protected abstract void doExercise(HttpServletRequest req, HttpServletResponse resp, RepoProblemAndTestCaseList exercise)
			throws ServletException, IOException;

}