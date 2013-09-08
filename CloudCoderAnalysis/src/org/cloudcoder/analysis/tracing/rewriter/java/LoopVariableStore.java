package org.cloudcoder.analysis.tracing.rewriter.java;

public class LoopVariableStore extends VariableStore
{
    protected int nestingDepth;
    protected int[] iterations;
    
    public LoopVariableStore(String lineNum, int depth, int... iterations) {
        super(lineNum);
        this.nestingDepth=depth;
        this.iterations=iterations;
    }
    
    

}
