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
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EditCodeServiceImpl extends RemoteServiceServlet implements EditCodeService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(EditCodeServiceImpl.class);

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
    	}
        
        return problem;
	}

    @Override
    public String loadCurrentText() throws NetCoderAuthenticationException {
    	// make sure client is authenticated
    	User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
    	
    	// make sure a problem has been loaded
    	Problem problem = (Problem) getThreadLocalRequest().getSession().getAttribute("problem");
    	
    	if (problem == null) {
    		// Can't load current text unless a Problem has been loaded
    		throw new NetCoderAuthenticationException();
    	}

    	String text = doLoadCurrentText(user, problem.getProblemId());
    	
    	// FIXME: this is only necessary because (for debugging purposes) LogCodeChangeServiceImpl expects to have the full document
    	TextDocument doc = new TextDocument();
    	doc.setText(text);
    	getThreadLocalRequest().getSession().setAttribute("doc", doc);
    	
    	return text;
    }

	protected String doLoadCurrentText(User user, int problemId) {
    	Change mostRecent = Database.getInstance().getMostRecentChange(user, problemId);

    	if (mostRecent == null) {
    		// Presumably, user has never worked on this problem.
    		logger.debug("No changes recorded for user " + user.getId() + ", problem " + problemId);
    		return "";
    	} else {
    		Change change = mostRecent; // result.get(0);

    		// If the Change is a full text change, great.
    		if (change.getType() == ChangeType.FULL_TEXT) {
    			return change.getText();
    		}

    		// Otherwise, find the last full-text change (if any) and
    		// apply all later changes.
    		
    		// Find the most recent full-text change.
    		Change fullText = Database.getInstance().getMostRecentFullTextChange(user, problemId);
    		
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
    		List<Change> deltaList = Database.getInstance().getAllChangesNewerThan(user, problemId, baseRev);
    		
    		// Apply the deltas to the base revision.
    		try {
        		ApplyChangeToTextDocument applicator = new ApplyChangeToTextDocument();
	    		for (Change delta : deltaList) {
	    			applicator.apply(delta, textDocument);
	    		}
	    		return textDocument.getText();
    		} catch (RuntimeException e) {
    			// FIXME: should do something smarter than this 
    			return fullText != null ? fullText.getText() : "";
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
		
		//
		// TODO: we don't really need to keep a complete copy
		// of the TextDocument in the session - this is mostly
		// for debugging
		//
		
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
		
		return true;
	}

}
