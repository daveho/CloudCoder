package org.cloudcoder.analysis.tracing.rewriter.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class GenericParser extends ASTVisitor
{
    protected CompilationUnit compilationUnit;
    
    protected static String readString(File file) throws IOException {
        StringBuffer buf=new StringBuffer();
        Scanner scan=new Scanner(new FileInputStream(file));
        while (scan.hasNextLine()) {
            String s=scan.nextLine();
            buf.append(s);
            buf.append("\n");
        }
        return buf.toString();
    }
    protected void setCompilationUnit(CompilationUnit cu) {
        this.compilationUnit=cu;
    }
    
    protected boolean hasAncestorOfType(ASTNode node, Class<?> type) {
        ASTNode temp=node;
        while (temp!=null) {
            if (type.isInstance(temp)) {
                return true;
            }
            temp=temp.getParent();
        }
        return false;
    }

    public void parse(File file) throws IOException {
        parse(readString(file));
    }

    public void parse(String text) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(text.toCharArray());    
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        setCompilationUnit(cu);
        cu.accept(this);
    }

    protected int getStartLine(ASTNode node) {
        return compilationUnit.getLineNumber(node.getStartPosition());
    }

    protected int getEndLine(ASTNode node) {
        return compilationUnit.getLineNumber(node.getStartPosition()+node.getLength());
                
    }
    
}
