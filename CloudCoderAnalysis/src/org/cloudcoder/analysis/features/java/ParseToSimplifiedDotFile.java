package org.cloudcoder.analysis.features.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ParseToSimplifiedDotFile extends ParseToDotFile
{

    @Override
    public boolean preVisit2(ASTNode node) {
        if (node instanceof VariableDeclarationStatement) {
            return process(node, node.toString());
        } else if (node instanceof VariableDeclarationExpression) {
            return process(node, node.toString());
        }
        return super.preVisit2(node);
    }
    
    @Override
    protected String getTextGraph() {
        String lookup="";
        //for (ASTNode node : reverseName.values()) {
        for (ASTNode node : map.keySet()) {
            String label=labelMap.get(node);
            if (node instanceof InfixExpression) {
                InfixExpression e=(InfixExpression)node;
                label=e.getOperator().toString();
            } else if (node instanceof PostfixExpression) {
                PostfixExpression e=(PostfixExpression)node;
                label=e.getOperator().toString();
            } else if (node instanceof SimpleName) {
                SimpleName n=(SimpleName)node;
                label=n.getIdentifier();
            } else if (node instanceof NumberLiteral) {
                NumberLiteral n=(NumberLiteral)node;
                label=n.getToken();
            } else if (node instanceof Block) {
                label="{";
            } else if (node instanceof CharacterLiteral) {
                label=node.toString();
            } else if (node instanceof PrimitiveType) {
                PrimitiveType t=(PrimitiveType)node;
                label=t.toString();
                //if (la)
            } else if (node instanceof SimpleType) {
                SimpleType t=(SimpleType)node;
                label=t.getName().toString();
            } else if (node instanceof Modifier) {
                label=node.toString();
            }
            lookup+=map.get(node)+" [label=\""+label+"\"];\n";
        }
        return lookup;
    }
    
}
