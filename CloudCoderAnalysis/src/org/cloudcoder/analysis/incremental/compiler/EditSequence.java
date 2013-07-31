package org.cloudcoder.analysis.incremental.compiler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.Change;

public class EditSequence implements Iterable<LineEdit>
{
    
    private List<Change> allChanges=new LinkedList<Change>();
    private List<LineEdit> lines=new LinkedList<LineEdit>();
    private LineEdit current=null;
    private Change lastChange=null;
    
    public Iterator<LineEdit> iterator() {
        return lines.iterator();
    }
    
    private void endCurrentLine() {
        if (current==null) {
            return;
        }
        lines.add(current);
        boolean foundNonWhitespace=false;
        for (Change c : current) {
            if (!c.getText().matches("\\s+")) {
                foundNonWhitespace=true;
                break;
            }
        }
        if (!foundNonWhitespace) {
            current.setType(LineEditType.WHITESPACE);
        }
        current=null;
        lastChange=null;
    }
    
    private void startNewLine(LineEditType lineEditType) {
        if (current!=null) {
            endCurrentLine();
        }
        current=new LineEdit();
        current.setType(lineEditType);
    }
    
    private void addChange(Change change) {
        allChanges.add(change);
        if (current==null) {
            current=new LineEdit();
        }
        current.add(change);
        lastChange=change;
    }
    
    public void parseChanges(List<Change> changeList) {
        /*
         *  Basically this is a state machine where we look at things like
         *  the type of change, the what line last change that was made,
         *  whether there was a last change, and so on.
         *  
         *  We use this information to group a list of Change events into
         *  a collection of meta-changes events we are calling "LineEdits".
         *  
         *  A LineEdit could be a multi-line cut or paste (i.e. insertion or deletion
         *  of more than 1 line), a series of related edits to a single line, 
         *  pure whitespace changes (addition or removal), or a "full text" change 
         *  (essentially a submission event).
         *  
         */
        for (Change c : changeList) {
            switch(c.getType()) {
            case FULL_TEXT:
                // We should make sure that the full_text and our current text match up
                // modulo the whitespace.  Otherwise we can ignore these, or add an empty
                // change operation to our collection of edits.
                startNewLine(LineEditType.FULL_TEXT);
                addChange(c);
                endCurrentLine();
                break;
            case INSERT_TEXT:
                if (c.getStartRow()==c.getEndRow()) {
                    // insert text into one line
                    if (lastChange==null) {
                        startNewLine(LineEditType.EDIT_LINE);
                        addChange(c);
                    } else {
                        //lastChange != null
                        if (c.getStartRow()==lastChange.getStartRow()) {
                            addChange(c);
                        } else {
                            // end the previous line of changes and start a new one
                            startNewLine(LineEditType.EDIT_LINE);
                            addChange(c);
                        }
                    }
                } else {
                    // insert text across several lines
                    // treat as a single insert operation
                    // and end the previous line of edits
                    startNewLine(LineEditType.ADD_LINES);
                    addChange(c);
                    endCurrentLine();
                }
                break;
            case INSERT_LINES:
//                if (c.getText().matches("\\s+")) {
//                    startNewLine(LineEditType.WHITESPACE);
//                } else {
//                    startNewLine(LineEditType.ADD_LINES);
//                }
                startNewLine(LineEditType.ADD_LINES);
                addChange(c);
                endCurrentLine();
                break;
            case REMOVE_TEXT:
                if (c.getStartRow()==c.getEndRow()) {
                    if (c.getText().equals("")) {
                        // Still not sure what to do with these null removes...
                        // perhaps skip for now?
//                    } else if (c.getText().matches("\\s+")) {
//                        startNewLine(LineEditType.WHITESPACE);
//                        addChange(c);
//                        endCurrentLine();
                    } else if (lastChange==null) {
                        startNewLine(LineEditType.EDIT_LINE);
                        addChange(c);
                    } else {
                        //lastChange != null
                        if (c.getStartRow()==lastChange.getStartRow()) {
                            addChange(c);
                        } else {
                            // end the previous line of changes and start a new one
                            startNewLine(LineEditType.EDIT_LINE);
                            addChange(c);
                        }
                    }
                } else {
                    // multi-line remove
                    startNewLine(LineEditType.REMOVE_LINES);
                    addChange(c);
                    endCurrentLine();
                }
                break;
            case REMOVE_LINES:
                startNewLine(LineEditType.REMOVE_LINES);
                addChange(c);
                endCurrentLine();
                break;
            default:
                throw new IllegalArgumentException("Unknown ChangeType: "+c.getType());
            }
        }
    }

    public int getNumChanges() {
        return allChanges.size();
    }

    public List<Change> getAllChanges() {
        return allChanges;
    }
}
