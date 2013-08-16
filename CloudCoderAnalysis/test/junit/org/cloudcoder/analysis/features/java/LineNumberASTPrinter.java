package junit.org.cloudcoder.analysis.features.java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.cloudcoder.analysis.tracing.rewriter.java.LineNumberParser;
import org.junit.Test;

public class LineNumberASTPrinter
{
    static final String inpath="testing/rewrite/java";
    static final String outpath="output";

    static void parse(String filename) throws IOException {
        

        File infile=new File(inpath+"/"+filename+".java");
        File outfile=new File(outpath+"/"+filename+".dot");
        LineNumberParser.parseFile(infile, outfile);
    }
    
    @Test
    public void testExercise1() throws Exception {
        parse("Exercise1");
    }
    
    @Test
    public void testVarChecker() throws Exception {
        parse("VarChecker");
    }

}
