package org.cloudcoder.analysis.features.java;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;


/**
 * Convert a program to an AST and print it to a file format
 * for a DOT file viewable with GraphViz.  This is primarily
 * useful for testing and debugging.
 * 
 * @author jaimespacco
 *
 */
public class ParseToDotFile extends ASTVisitor
{
    private int nodes=0;
    protected Map<ASTNode,String> map=new LinkedHashMap<ASTNode,String>();
    protected Map<ASTNode,String> labelMap=new HashMap<ASTNode,String>();
    //protected Map<String,ASTNode> reverseName=new TreeMap<String,ASTNode>();
    protected StringBuffer dotfile=new StringBuffer();
    
    protected static String readFile(String filename) throws IOException 
    {
        String res="";
        Scanner scan=new Scanner(new FileInputStream(filename));
        while (scan.hasNextLine()) {
            String line=scan.nextLine();
            res+=line+"\n";
        }
        return res;
    }

    public void parseProgramTextDotFile(String programText, String outfile)
    throws IOException
    {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(programText.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        //ParseToDotFile visitor=new ParseToDotFile();
        cu.accept(this);
        PrintStream out=new PrintStream(new FileOutputStream(outfile));
        out.print(getDotFormatString());
        out.flush();
        out.close();
    }
    
    public void parseToFullDotFile(String infile, String outfile)
    throws IOException
    {
        String src = readFile(infile);
        
        parseProgramTextDotFile(src, outfile);
    }
    
//    private String getTypeGraph() {
//        String lookup="";
//        for (ASTNode node : reverseName.values()) {
//            String label=names.get(node);
//            lookup+=map.get(node)+" [label=\""+label+"\"];\n";
//        }
//        return lookup;
//    }
    
    protected String getTextGraph() {
        String lookup="";
        for (Map.Entry<ASTNode,String> entry : labelMap.entrySet()) {
            lookup+=map.get(entry.getKey())+" [label=\""+entry.getValue()+"\"];\n";
        }
        return lookup;
    }
    
    public String getDotFormatString() {
        return "digraph ast {\n" +
            getTextGraph()+"\n"+
            dotfile +"\n"+
            "}";
    }
    
    protected static String getTypeName(Class cl) {
        return cl.getSimpleName();
    }
        
    protected void createOrLookupNode(ASTNode node, String text){
        text=text.replaceAll("\n", "");
        if (!map.containsKey(node)) {
            String nodeName=String.format("n%03d", nodes);
            map.put(node, nodeName);
            labelMap.put(node, text);
            //reverseName.put(nodeName, node);
            nodes++;
        }
    }
    
    protected void createOrLookupNode(ASTNode node) {
        createOrLookupNode(node, getTypeName(node.getClass()));
    }
    
    protected boolean process(ASTNode node, String text) {
        createOrLookupNode(node, text);
        ASTNode parent=node.getParent();
        if (parent==null) {
            return true;
        }
        createOrLookupNode(parent);
        dotfile.append(map.get(parent)+" -> "+map.get(node)+"\n");
        return false;
    }
    
    protected boolean process(ASTNode node) {
        createOrLookupNode(node);
        ASTNode parent=node.getParent();
        if (parent==null) {
            return true;
        }
        createOrLookupNode(parent);
        dotfile.append(map.get(parent)+" -> "+map.get(node)+"\n");
 
        return true;
    }
    
    @Override
    public boolean preVisit2(ASTNode node) {
        return process(node);
    }
    
    
}
