package junit.org.cloudcoder.analysis.features.java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.cloudcoder.analysis.tracing.rewriter.java.BlockScope;
import org.cloudcoder.analysis.tracing.rewriter.java.LineNumberParser;
import org.cloudcoder.analysis.tracing.rewriter.java.StatementInstrumenter;
import org.cloudcoder.analysis.tracing.rewriter.java.VariableBlockFinder;
import org.junit.Test;

public class TestInstrumenter
{

    static final String inpath="testing/rewrite/java";
    static final String outpath="output";
    
    @Test
    public void testVarChecker() throws Exception {
        String filename="VarChecker";
        
        File infile=new File(inpath+"/"+filename+".java");
        VariableBlockFinder finder=new VariableBlockFinder();
        finder.addMethodToInstrument("max");
        finder.parse(infile);
        for (Entry<Integer,Set<String>> entry : finder.getVariableMap().entrySet()) {
            System.out.println(entry.getKey()+" => "+entry.getValue());
        }
        /*
        for (BlockScope scope : finder.getBlockScopes()) {
            System.out.println(scope);
        }
        */
    }
    
    @Test
    public void testStatementInstrumenter() throws Exception {
        String filename="VarChecker";
        
        File infile=new File(inpath+"/"+filename+".java");
        StatementInstrumenter instr=new StatementInstrumenter();
        instr.addInstrumentedMethod("max");
        instr.parse(infile);
        System.out.println(instr.getCode());
    }

}
