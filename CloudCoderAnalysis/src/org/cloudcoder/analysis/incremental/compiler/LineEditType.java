package org.cloudcoder.analysis.incremental.compiler;

public enum LineEditType {
    /** Edit one line of code */
    EDIT_LINE,
    
    /** Adds more than 1 line */
    ADD_LINES,
    
    /** Deletes more than 1 line */
    REMOVE_LINES,
    
    /** Changes only to the whitespace */
    WHITESPACE,
    
    /** FULL_TEXT change */
    FULL_TEXT,
}
