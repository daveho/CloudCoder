package org.cloudcoder.analysis.incremental.compiler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.ApplyChangeToTextDocument;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.TextDocument;

public class LineEdit implements Iterable<Change>
{
    private List<Change> changes=new LinkedList<Change>();
    /*
     * Improvements:
     * 
     * Mark line changes with a type:
     *      add multiple lines
     *      remove multiple lines
     *      net add
     *      net remove
     *      whitespace only
     *      
     * 
     */
    private LineEditType type;
    

    public Iterator<Change> iterator() {
        return changes.iterator();
    }
    
    public void add(Change c) {
        changes.add(c);
    }
    public void apply(TextDocument doc) {
        ApplyChangeToTextDocument apply=new ApplyChangeToTextDocument();
        for (Change c : changes) {
            apply.apply(c, doc);
        }
    }
    
    public String toString() {
        if (changes.size()>1&&changes.get(0).getStartRow()!=changes.get(changes.size()-1).getStartRow()) {
            throw new IllegalStateException("ERROR! edits to line "+changes.get(0).getStartRow()+" and to line "+changes.get(changes.size()-1).getStartRow()+" in the same edit");
        }
        return type+" at line "+(changes.get(0).getStartRow()+1);
    }
    public LineEditType getType() {
        return type;
    }
    public void setType(LineEditType type) {
        this.type = type;
    }
}
