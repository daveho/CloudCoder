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
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class CheckCode extends ASTVisitor
{
    private CompilationUnit compilationUnit;
    
    public void setCompilationUnit(CompilationUnit cu) {
        this.compilationUnit=cu;
    }
    
    public int getStartLine(ASTNode node) {
        return compilationUnit.getLineNumber(node.getStartPosition());
    }
    
    public int getEndLine(ASTNode node) {
        return compilationUnit.getLineNumber(node.getStartPosition()+node.getLength());
    }

    
    
    @Override
    public boolean preVisit2(ASTNode node) {
        int start=getStartLine(node);
        int end=getEndLine(node);
        if (node instanceof Statement) {
            System.out.println(node.getClass()+" IS-A Statement!");
            System.out.println(node.getClass()+" "+start+" "+end);
        }
        return super.preVisit2(node);
    }
    
    @Override
    public boolean visit(DoStatement node) {
        System.out.println("Visit: "+node);
        return super.visit(node);
    }

    @Override
    public boolean visit(ForStatement node) {
        System.out.println("Visit: "+node);
        return super.visit(node);
    }

    @Override
    public boolean visit(IfStatement node) {
        System.out.println("Visit: "+node);
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        //System.out.println("Visit: "+node);
        System.out.println("Method name: "+node.getName());
        
        System.out.println("Start line: "+getStartLine(node));
        System.out.println("End line: "+getEndLine(node));
        
        return super.visit(node);
    }

    @Override
    public boolean visit(WhileStatement node) {
        System.out.println("Visit: "+node);
        return super.visit(node);
    }

    private static String readString(File file) throws IOException {
        StringBuffer buf=new StringBuffer();
        Scanner scan=new Scanner(new FileInputStream(file));
        while (scan.hasNextLine()) {
            String s=scan.nextLine();
            buf.append(s);
            buf.append("\n");
        }
        return buf.toString();
    }
    
    public void process(File file) throws IOException {
        process(readString(file));
    }
    
    public void process(String text) {
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
}
