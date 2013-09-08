package org.cloudcoder.analysis.tracing.rewriter.java;

import java.util.ArrayList;
import java.util.Iterator;

public class CodePath implements Iterable<VariableStore>
{
    /*
     * TODO: Should VariableStores act more like a linked list
     *      that know the next frame?
     * XXX: How do we track the start and ends of loops?
     * 
     * 
     */
    private VariableStore currentVariableStore;
    private VariableStore previousVariableStore;
    private ArrayList<VariableStore> lineChain=new ArrayList<VariableStore>();
    
    public void startNewLine(int lineNum, int... loopCounters) {
        if (currentVariableStore!=null) {
            endLine();
        }
        String lineNumStr=makeLineNum(lineNum, loopCounters);
        boolean isLoop=false;
        int depth=0;
        for (int count : loopCounters) {
            if (count>0) {
                isLoop=true;
                depth++;
            }
        }
        if (isLoop) {
            currentVariableStore=new LoopVariableStore(lineNumStr, depth, loopCounters);
        } else {
            currentVariableStore=new VariableStore(lineNumStr);
        }
    }
    
    public void endLine() {
        // TODO: Check the previous varstore, so we can check 
        // which values have actually changed.
        lineChain.add(currentVariableStore);
        currentVariableStore=previousVariableStore;
    }
    
    public void addVariable(String name, Object value) {
        if (currentVariableStore==null ) {
            throw new IllegalStateException("currentVariableStore is null; "+
                    "cannot map a variable name/value");
        }
        if (currentVariableStore==previousVariableStore) {
            throw new IllegalStateException("currentVariableStore same as previousVariableStore; "+
                    "was endLine() called?");
        }
        currentVariableStore.put(name, value);
    }
    
    
    
    private void add(VariableStore store) {
        lineChain.add(store);
    }

    @Override
    public Iterator<VariableStore> iterator() {
        return lineChain.iterator();
    }
    
    public String toString() {
        StringBuffer buf=new StringBuffer();
        for (VariableStore v : lineChain) {
            buf.append(v+"\n");
        }
        return buf.toString();
    }

    public static VariableStore createVariableStore(int lineNum, int... loopCounters) {
        String lineNumStr=makeLineNum(lineNum, loopCounters);
        boolean isLoop=false;
        int depth=0;
        for (int count : loopCounters) {
            if (count>0) {
                isLoop=true;
                depth++;
            }
        }
        if (isLoop) {
            return new LoopVariableStore(lineNumStr, depth, loopCounters);
        }
        return new VariableStore(lineNumStr);
    }

    static String makeLineNum(int lineNum, int... loopCounters) {
        String res=""+lineNum;
        for (int count : loopCounters) {
            if (count==0) {
                return res;
            }
            res+="-"+count;
        }
        return res;
    }

}
