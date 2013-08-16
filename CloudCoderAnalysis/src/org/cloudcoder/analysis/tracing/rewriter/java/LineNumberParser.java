package org.cloudcoder.analysis.tracing.rewriter.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;

public class LineNumberParser extends GraphVizParser
{
    public boolean preVisit2(ASTNode node) {
        process(node, getTypeName(node.getClass())+" "+getStartLine(node)+" "+getEndLine(node));
        return super.preVisit2(node);
    }
    
    public static void parseFile(File infile, File outfile) throws IOException {
        LineNumberParser parser=new LineNumberParser();
        parser.parse(infile);
        FileOutputStream fos=new FileOutputStream(outfile);
        IOUtils.write(parser.getDotFormatString(), fos);
        fos.flush();
        fos.close();
    }
}
