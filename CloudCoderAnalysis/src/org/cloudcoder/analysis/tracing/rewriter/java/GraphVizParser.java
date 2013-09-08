package org.cloudcoder.analysis.tracing.rewriter.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class GraphVizParser extends GenericParser
{
    private Map<ASTNode, String> nodeNameMap=new LinkedHashMap<ASTNode, String>();
    private Map<ASTNode, String> labelMap=new LinkedHashMap<ASTNode, String>();
    private int nodes=1;
    protected StringBuffer dotfile=new StringBuffer();
    
    
    
    
   
    
    protected void createOrLookupNode(ASTNode node, String text){
        text=text.replaceAll("\n", "");
        if (!nodeNameMap.containsKey(node)) {
            String nodeName=String.format("n%03d", nodes);
            nodeNameMap.put(node, nodeName);
            labelMap.put(node, text);
            //reverseName.put(nodeName, node);
            nodes++;
        }
    }
    
    protected void createOrLookupNode(ASTNode node) {
        createOrLookupNode(node, getTypeName(node.getClass()));
    }
    static String getTypeName(Class<?> cl) {
        return cl.getSimpleName();
    }
    
    public String getDotFormatString() {
        return "digraph ast {\n" +
            getTextGraph()+"\n"+
            dotfile +"\n"+
            "}";
    }
    protected String getTextGraph() {
        String lookup="";
        for (Map.Entry<ASTNode,String> entry : labelMap.entrySet()) {
            lookup+=nodeNameMap.get(entry.getKey())+" [label=\""+entry.getValue()+"\"];\n";
        }
        return lookup;
    }
    
    protected boolean process(ASTNode node, String text) {
        createOrLookupNode(node, text);
        ASTNode parent=node.getParent();
        if (parent==null) {
            return true;
        }
        createOrLookupNode(parent);
        dotfile.append(nodeNameMap.get(parent)+" -> "+nodeNameMap.get(node)+"\n");
        return false;
    }
    protected boolean process(ASTNode node) {
        createOrLookupNode(node);
        ASTNode parent=node.getParent();
        if (parent==null) {
            return true;
        }
        createOrLookupNode(parent);
        dotfile.append(nodeNameMap.get(parent)+" -> "+nodeNameMap.get(node)+"\n");
 
        return true;
    }
}
