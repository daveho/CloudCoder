package org.cloudcoder.analysis.tracing.rewriter.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

public class StatementInstrumenter extends GenericParser
{
    /*
     * XXX better to use endVisit() methods to decrement indentation?
     *  probably not!
     * 
     * TODO re-write return statements
     *  How to handle these in the UI?
     *      
     * TODO include import statements for mapping classes
     * 
     * TODO method to reset static instance variables
     *  since we are running in separate threads, 
     *  there may be shared state 
     * 
     * TODO how to track the names of the loop counters?
     *  UI will need to know the difference between real variables
     *  and synthetic loop counter variables
     * 
     */
    
    protected List<MyStatement> statements=new LinkedList<MyStatement>();

    protected int indentation=0;
    
    protected Set<String> methodsToRewrite=new HashSet<String>();
    
    protected Map<Integer, Set<String>> varmap;
    
    protected int numInstrumentedMethods=0;
    protected int numInstrumentedLoops=0;
    protected ArrayList<String> loopCounters=new ArrayList<String>();
    protected String currentMethodName;
    
    private String getNextMethodName() {
        numInstrumentedMethods++;
        currentMethodName="___codepath"+numInstrumentedMethods;
        return currentMethodName;
    }
    //TODO better way of tracking names for loop counters
    private String getNextLoopName() {
        numInstrumentedLoops++;
        loopCounters.add("___loop"+numInstrumentedLoops);
        return getCurrentLoopName();
    }
    private String getCurrentLoopName() {
        return loopCounters.get(loopCounters.size()-1);
    }
    
    private enum StatementType {
        FOR_LOOP_HEADER,
        WHILE_LOOP_HEADER,
        METHOD_HEADER,
        RETURN,
        OTHER,
        UNINSTRUMENTABLE_TEXT
    }
    
    private class MyStatement {
        //XXX could use inheritance for StatementTypes
        final String text;
        final int indentation;
        final int nestingDepth;
        final int startLine;
        final int endLine;
        final StatementType type;
        public MyStatement(String stmt, int indentation, int depth, int startLine, int endLine, StatementType type) {
            this.text=stmt;
            this.indentation=indentation;
            this.nestingDepth=depth;
            this.startLine=startLine;
            this.endLine=endLine;
            this.type=type;
        }
        public MyStatement(String stmt) {
            this(stmt, -1, -1, -1, -1, StatementType.UNINSTRUMENTABLE_TEXT);
        }
        private String indent(int indent) {
            StringBuffer buf=new StringBuffer();
            for (int i=0; i<indent*4; i++) {
                buf.append(' ');
            }
            return buf.toString();
        }
        public String toString() {
            return indent(indentation)+text;
        }

        public String toInstrumentedString() {
            
            String statement=toString();
            
            String nameOfCodePath=currentMethodName;
            
            if (type==StatementType.METHOD_HEADER) {
                //prepend the static tracking instance variables *before* the method header
                // this makes them static variables
                StringBuffer preheader=new StringBuffer();
                nameOfCodePath=getNextMethodName();
                preheader.append("public static CodePath "+nameOfCodePath+" = new CodePath();\n");
                preheader.append("public static void reset"+nameOfCodePath+"() {\n");
                preheader.append(nameOfCodePath+" = new CodePath();\n");
                preheader.append("\n}");
                statement=preheader.toString()+'\n'+statement;
            }
            
            // stringbuffer to build the tracking for each line
            StringBuffer buf=new StringBuffer();
            
            buf.append(nameOfCodePath+".startNewLine("+startLine);
            
            // put in tracking for all of the variables
            if (type==StatementType.FOR_LOOP_HEADER) {
                //TODO create for loop iteration tracker
                // check the nestingDepth
            } else if (type==StatementType.WHILE_LOOP_HEADER) {
                //TODO create while loop iter tracker
                // check the nestingDepth
            }
            
            buf.append(");\n");
            
            Set<String> liveVars=getLiveVars();
            if (liveVars==null) {
                // This probably should not happen...
                return toString();
            }
            
            
            
            
            
            for (String varname : liveVars) {
                buf.append(nameOfCodePath+".addVariable(\""+varname+"\", "+varname+");\n");
            }
            buf.append(nameOfCodePath+".endLine();\n");
            
            String instrumentation=buf.toString();
            
            if (type==StatementType.RETURN) {
                return instrumentation+statement;
            }
            return statement+'\n'+instrumentation;
        }
        private Set<String> getLiveVars() {
            if (type==StatementType.FOR_LOOP_HEADER ||
                    type==StatementType.WHILE_LOOP_HEADER ||
                    type==StatementType.METHOD_HEADER)
            {
                // only get the live 
                return varmap.get(startLine);
            }
            for (int i=startLine; i<=endLine; i++) {
                if (varmap.containsKey(i)) {
                    return varmap.get(i);
                }
            }
            //XXX should probably never happen
            return null;
        }
    }
    
    
    
    private static int getNestingDepth(ASTNode node) {
        int depth=0;
        ASTNode temp=node;
        while (temp!=null) {
            if (temp instanceof ForStatement ||
                    temp instanceof EnhancedForStatement ||
                    temp instanceof WhileStatement)
            {
                depth++;
            }
            temp=temp.getParent();
        }
        return depth;
    }
    
    public String getCode() {
        StringBuffer buf=new StringBuffer();
        buf.append("import org.cloudcoder.analysis.tracing.rewriter.java.CodePath;\n");
        for (MyStatement s : statements) {
            if (s.type==StatementType.UNINSTRUMENTABLE_TEXT) {
                buf.append(s.toString());
                buf.append('\n');
            } else {
                buf.append(s.toInstrumentedString());
                buf.append('\n');
            }
        }
        return buf.toString();
    }
    
    private void acceptWithIndent(ASTNode node) {
        indentation++;
        node.accept(this);
        indentation--;
    }
    
    static StatementType getType(ASTNode node) {
        if (node instanceof ForStatement || 
                node instanceof EnhancedForStatement)
        {
            return StatementType.FOR_LOOP_HEADER;
        }
        if (node instanceof WhileStatement) {
            return StatementType.WHILE_LOOP_HEADER;
        }
        if (node instanceof MethodDeclaration) {
            return StatementType.METHOD_HEADER;
        }
        if (node instanceof ReturnStatement) {
            return StatementType.RETURN;
        }
        return StatementType.OTHER;
    }
    
    private void add(ASTNode node) {
        int nestingDepth=getNestingDepth(node);
        int startLine=getStartLine(node);
        int endLine=getEndLine(node);
        statements.add(new MyStatement(node.toString(), indentation, nestingDepth, startLine, endLine, getType(node)));
    }
    
    private void add(ASTNode node, String text) {
        int nestingDepth=getNestingDepth(node);
        int startLine=getStartLine(node);
        int endLine=getEndLine(node);
        statements.add(new MyStatement(text, indentation, nestingDepth, startLine, endLine, getType(node)));
    }
    
    private void add(String text) {
        statements.add(new MyStatement(text));
    }
    
    public void addInstrumentedMethod(String name) {
        this.methodsToRewrite.add(name);
    }
    
    public void parse(String text) {
        // TODO: awkward workflow: need to add methods to rewrite
        // or else parse doesn't work properly
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(text.toCharArray());    
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        this.compilationUnit = (CompilationUnit) parser.createAST(null);
        
        VariableBlockFinder finder=new VariableBlockFinder();
        finder.addMethodToInstrument(this.methodsToRewrite);
        finder.setCompilationUnit(this.compilationUnit);
        compilationUnit.accept(finder);
        this.varmap=finder.getVariableMap();
        //XXX debugging
        for (Map.Entry<Integer,Set<String>> entry : varmap.entrySet()) {
            //System.err.println(entry.getKey()+" => "+entry.getValue());
        }
        
        compilationUnit.accept(this);
    }
    
    public void parse(File file) throws IOException {
        this.parse(readString(file));
    }

    @Override
    public boolean visit(Block node) {
        for (int i=0; i<node.statements().size(); i++) {
            Statement statement = (Statement)node.statements().get(i);
            if (statement instanceof EnhancedForStatement) {
                statement.accept(this);
            } else if (statement instanceof ForStatement) {
                statement.accept(this);
            } else if (statement instanceof WhileStatement) {
                statement.accept(this);
            } else if (statement instanceof IfStatement) {
                statement.accept(this);
            } else {
                add(statement);
            }
        }
        return false;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        String fortext="for ("+
                node.getParameter()+" : "+
                node.getExpression()+") {";
        add(node, fortext);
        acceptWithIndent(node);
        add("}");
        return false;
    }
    
    private static String createList(List list) {
        StringBuffer buf=new StringBuffer();
        int size=list.size();
        for (int i=0; i<size-1; i++) {
            Object o=list.get(i);
            buf.append(o);
            buf.append(",");
            buf.append(" ");
        }
        if (size>0) {
            buf.append(list.get(size-1));
        }
        return buf.toString();
    }

    @Override
    public boolean visit(ForStatement node) {
        String fortext="for ("+
                createList(node.initializers())+
                "; "+
                node.getExpression()+
                "; "+
                createList(node.updaters())+
                ") {";
        add(node, fortext);
        // don't visit the initializers, expression or updaters
        acceptWithIndent(node.getBody());
        add("}");
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        String iftext="if ("+
                node.getExpression()+
                ") {";
        add(node, iftext);

        acceptWithIndent(node.getThenStatement());
        
        if (node.getElseStatement()!=null) {
            //XXX should this just be a regular add()?
            add(node, "} else {");
            acceptWithIndent(node.getElseStatement());
        }
        add("}");
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (!methodsToRewrite.contains(node.getName().toString())) {
            // we aren't supposed to re-write this method
            add(node, node.toString());
            return false;
        }
        
        //TODO get correct modifiers
        String header="public static "+
                node.getReturnType2()+" "+
                node.getName()+"(";
        header+=createList(node.parameters());
        header+=") {";
        
        //TODO figure out the start/end of just the method header
        add(node, header);
        acceptWithIndent(node.getBody());
        add("}");
        
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        String text="while ("+node.getExpression()+") {";
        add(node, text);
        acceptWithIndent(node.getBody());
        add("}");
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        add("public class "+node.getName()+" {");
        //XXX What about (ugh!) static initializers?
        //XXX generic type parameters?
        //XXX visibility modifiers?
        //XXX inner classes and other crap like that?
        
        for (FieldDeclaration f : node.getFields()) {
            add(f);
        }
        for (MethodDeclaration m : node.getMethods()) {
            acceptWithIndent(m);
        }
        add("}");
        return false;
    }
    
    
}
