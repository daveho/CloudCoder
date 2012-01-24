package org.cloudcoder.app.server.rpc;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.EditCodeService;
import org.cloudcoder.app.server.model.ApplyChangeToTextDocument;
import org.cloudcoder.app.server.model.TextDocument;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.IContainsEvent;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EditCodeServiceImpl extends RemoteServiceServlet implements EditCodeService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(EditCodeServiceImpl.class);
	
	private static final boolean DEBUG_CODE_DELTAS = false;

	@Override
	public Problem setProblem(int problemId) throws NetCoderAuthenticationException {
		// make sure client is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());

		// Get the problem
		Problem problem = Database.getInstance().getProblem(user, problemId);

		if (problem != null) {
			// Store the Problem in the HttpSession - that way, the servlets
			// that depend on knowing the problem have access to a known-authentic
			// problem. (I.e., we don't have to trust a problem id sent as
			// an RPC parameter which might have been forged.)
			getThreadLocalRequest().getSession().setAttribute("problem", problem);

			// If appropriate, record that the user has started the problem
			Database.getInstance().getOrAddLatestSubmissionReceipt(user, problem);
		}

		return problem;
	}

    @Override
    public ProblemText loadCurrentText() throws NetCoderAuthenticationException {
    	// make sure client is authenticated
    	User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
    	
    	// make sure a problem has been loaded
    	Problem problem = (Problem) getThreadLocalRequest().getSession().getAttribute("problem");
    	
    	if (problem == null) {
    		// Can't load current text unless a Problem has been loaded
    		throw new NetCoderAuthenticationException();
    	}

    	ProblemText text = doLoadCurrentText(user, problem);
    	
    	if (DEBUG_CODE_DELTAS) {
	    	// Keep a TextDocument in the session for debugging code deltas
	    	TextDocument doc = new TextDocument();
	    	doc.setText(text.getText());
	    	getThreadLocalRequest().getSession().setAttribute("doc", doc);
    	}
    	
    	return text;
    }

	protected ProblemText doLoadCurrentText(User user, Problem problem) {
    	Change mostRecent = Database.getInstance().getMostRecentChange(user, problem.getProblemId());

    	if (mostRecent == null) {
    		// Presumably, user has never worked on this problem.
    		logger.debug("No changes recorded for user " + user.getId() + ", problem " + problem.getProblemId());
    		
    		// If the problem has a skeleton, it is the initial problem text.
    		// Otherwise, just use the empty string.
    		String initialText = problem.hasSkeleton() ? problem.getSkeleton() : "";
    		ProblemText initialProblemText = new ProblemText(initialText, true);
    		
    		return initialProblemText;
    	} else {
    		Change change = mostRecent; // result.get(0);

    		// If the Change is a full text change, great.
    		if (change.getType() == ChangeType.FULL_TEXT) {
    			return new ProblemText(change.getText(), false);
    		}

    		// Otherwise, find the last full-text change (if any) and
    		// apply all later changes.
    		
    		// Find the most recent full-text change.
    		Change fullText = Database.getInstance().getMostRecentFullTextChange(user, problem.getProblemId());
    		
    		// Text doc to accumulate changes.
    		TextDocument textDocument = new TextDocument();
    		
    		// Find the base revision (event id) that the deltas are relative to, if any.
    		int baseRev;
    		if (fullText != null) {
    			// found a full-text change to use as a base revision
    			textDocument.setText(fullText.getText());
    			baseRev = fullText.getEventId();
    		} else {
    			// no full-text change exists: base revision is implicitly the empty document
    			baseRev = -1;
    		}
    		
    		// Get all deltas that follow the base revision.
    		List<Change> deltaList = Database.getInstance().getAllChangesNewerThan(user, problem.getProblemId(), baseRev);
    		
    		// Apply the deltas to the base revision.
    		try {
        		ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();
	    		for (Change delta : deltaList) {
	    			applicator.apply(delta, textDocument);
	    		}
	    		return new ProblemText(textDocument.getText(), false);
    		} catch (RuntimeException e) {
    			// FIXME: should do something smarter than this 
    			return new ProblemText(fullText != null ? fullText.getText() : "", false);
    		}
    	}
	}

	@Override
	public Boolean logChange(Change[] changeList) throws NetCoderAuthenticationException {
		// make sure client is authenticated
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		
		// Make sure all Changes have proper user id
		for (IContainsEvent change : changeList) {
			if (change.getEvent().getUserId() != user.getId()) {
				throw new NetCoderAuthenticationException();
			}
		}

		// Insert changes
		Database.getInstance().storeChanges(changeList);
		
		if (DEBUG_CODE_DELTAS) {
			// For debugging - keep a TextDocument in the session,
			// and apply changes to it
			
			HttpServletRequest req = this.getThreadLocalRequest();
			HttpSession session = req.getSession();
			
			TextDocument doc = (TextDocument) session.getAttribute("doc");
			if (doc == null) {
				doc = new TextDocument();
				session.setAttribute("doc", doc);
			}
			ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();
			for (Change change : changeList) {
				applicator.apply(change, doc);
			}
			
			logger.debug("Document is now:\n" + doc.getText());
		}
		
		return true;
	}

}
