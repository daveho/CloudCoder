package org.cloudcoder.analysis.tracing.rewriter.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BlockScope {
    final int startLine;
    final int endLine;
    Set<String> variables;
    BlockScope(int s, int e) {
        this.startLine=s;
        this.endLine=e;
        variables=new LinkedHashSet<String>();
    }
    void add(String varName) {
        variables.add(varName);
    }
    void addAll(Collection<String> vars) {
        variables.addAll(vars);
    }
    
    public String toString() {
        StringBuffer buf=new StringBuffer();
        buf.append(startLine+" to "+endLine+": ");
        for (String s : variables) {
            buf.append(s);
            buf.append(" ");
        }
        return buf.toString();
    }
}