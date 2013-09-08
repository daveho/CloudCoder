package org.cloudcoder.analysis.tracing.rewriter.java;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

public class VariableBlockFinder extends GenericParser
{
    /*
     * Necessary to track things per block in addition to per line
     * since we need to know what is live at each line number.
     */
    Map<Integer,Set<String>> liveVarMap=new TreeMap<Integer,Set<String>>();
    private Stack<BlockScope> scopeStack=new Stack<BlockScope>();
    protected Set<String> methodsToRewrite=new LinkedHashSet<String>();
    
    private void addVar(int line, String name) {
        if (!liveVarMap.containsKey(line)) {
            liveVarMap.put(line, new LinkedHashSet<String>());
        }
        liveVarMap.get(line).add(name);
        // now merge in all of the other variables already visible 
        // in this scope and prior scopes
        
        
    }
    
    public void addMethodToInstrument(String name) {
        this.methodsToRewrite.add(name);
    }
    
    public void addMethodToInstrument(Set<String> set) {
        this.methodsToRewrite.addAll(set);
    }
    
    public Map<Integer, Set<String>> getVariableMap() {
        return this.liveVarMap;
    }
    
    @Override
    public boolean visit(Block node) {
        if (Initializer.class.isInstance(node.getParent())) {
            // ignore static initializers
            return false;
        }
        if (!(node.getParent() instanceof MethodDeclaration ||
                node.getParent() instanceof ForStatement ||
                node.getParent() instanceof EnhancedForStatement ||
                node.getParent() instanceof WhileStatement)) {
            // method declarations and loop headers:
            // these will already have a new scope, which is needed
            // to capture the parameters and loop variables
            scopeStack.push(new BlockScope(getStartLine(node), getEndLine(node)));
        }
        for (int i=0; i<node.statements().size(); i++) {
            Statement stmt = (Statement)node.statements().get(i);
            // merge existing visible variables into current scope
            // before we visit children
            int line=getStartLine(stmt);
            if (!liveVarMap.containsKey(line)) {
                liveVarMap.put(line, new LinkedHashSet<String>());
            }
            for (int j=0; j<scopeStack.size(); j++) {
                // totally pretending that stack is a list
                // which it (somewhat incorrectly) is in Java
                Set<String> set=liveVarMap.get(line);
                BlockScope scope=scopeStack.get(j);
                set.addAll(scope.variables);
                //liveVarMap.get(line).addAll(scopeStack.get(j).variables);
            }
            stmt.accept(this);
        }
        return false;
    }

    @Override
    public void endVisit(Block node) {
        // pop the most recent stack and add it to the list
        // of scopes we have processed
        if (Initializer.class.isInstance(node.getParent())) {
            // ignore static initializers
            return;
        }
        scopeStack.pop();
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (this.methodsToRewrite.contains(node.getName().toString())) {
            // only instrument methods we are asked to instrument
            BlockScope scope=new BlockScope(getStartLine(node), getEndLine(node));
            scopeStack.push(scope);
            return true;
        }
        return false;
    }
    
    

    @Override
    public boolean visit(DoStatement node) {
        scopeStack.push(new BlockScope(getStartLine(node), getEndLine(node)));
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        scopeStack.push(new BlockScope(getStartLine(node), getEndLine(node)));
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        scopeStack.push(new BlockScope(getStartLine(node), getEndLine(node)));
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        scopeStack.push(new BlockScope(getStartLine(node), getEndLine(node)));
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
        if (hasAncestorOfType(node, FieldDeclaration.class)) {
            return false;
        }
        //TODO re-write vardecs with no initializers,
        // like "int x;" or "String str;" so that they 
        // set default values for the type
        
        //TODO check for a for loop before a block
        // in case the initializers have been split onto multiple lines
        
        //XXX should we check for the start line of the next Statement up the tree?
        addVar(getStartLine(node), node.getName().toString());
        
        scopeStack.peek().add(node.getName().toString());
        return true;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        //XXX do these only happen with method parameters?
        // First, find line where the method starts
        int startLine=getStartLine(node);
        ASTNode temp=node;
        while (temp!=null) {
            if (temp instanceof MethodDeclaration) {
                startLine=getStartLine(temp);
                break;
            }
            temp=temp.getParent();
        }
        addVar(startLine,node.getName().toString());
        
        scopeStack.peek().add(node.getName().toString());
        return true;
    }
}
