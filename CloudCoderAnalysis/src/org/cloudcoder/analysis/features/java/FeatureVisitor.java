package org.cloudcoder.analysis.features.java;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class FeatureVisitor extends ASTVisitor
{

    public static TreeMap<String,Integer> map = new TreeMap<String,Integer>();
    public static LinkedList<Feature> features = new LinkedList<Feature>();
    public static TreeMap<String,Feature> featureMap;

    public void setFeatures(TreeMap<String, Feature> f) {
        featureMap = f;
        prepareFeatures();
    }

    /**
     * Extract features from Java code
     * 
     * @param s The source code as a String
     * @return A map of features and the counts of the number
     *      of times they appear.
     * @throws IOException
     */
    public HashMap<Feature,Integer> extractFeatures(String s)
            throws IOException
            {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(s.toCharArray());    
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(this);

        return getResults();
            }

    /**
     * 
     * @param s Submission
     * @return 
     * @throws IOException
     */
    public HashMap<Feature,Integer> extractFeatures(Submission s) throws IOException
    {
        //TODO:  Maybe change the return type to return
        // a list of features?

        String str=s.getSource();
        return extractFeatures(str);
    }

    public void putInMap(String name){
        if(map.containsKey(name)){
            int n = map.get(name);
            map.put(name,n+1);
        } else
            map.put(name,1);
    }

    public void decInMap(String name){
        int n = map.get(name);
        map.put(name,n-1);
    }

    public void putInMap(Feature f){
        features.add(f);
    }

    public HashMap<Feature,Integer> getResults(){
        HashMap<Feature,Integer> retVal = new HashMap<Feature, Integer>();

        for(Feature f : features){
            int n = map.get(f.getName());
            retVal.put(f,n);
        }

        return retVal;
    }

    public void prepareFeatures(){
        for(String s : featureMap.keySet())
            map.put(s,0);
    }

    private String getParent(ASTNode node) {
        String parent;
        if(node.getParent().toString().startsWith("{"))
            parent = node.getParent().getParent().toString();
        else
            parent = node.getParent().toString();
        return parent.substring(0, parent.indexOf(' '));
    }

    private boolean hasThisParent(ASTNode node,String name) {
        ASTNode parent = node.getParent();
        if(parent == null)
            return false;
        String parentName = parent.getClass().getSimpleName();
        if(parentName.equals(name))
            return true;
        return hasThisParent(parent,name);
    }

    public boolean isInteger( String input )
    {
        try {
            Integer.parseInt(input);
            return true;
        } catch(Exception e) {
            return false;
        }

    }

    public boolean isLogicalExp(String s){
        if(s.contains(">") || s.contains("=") || s.contains("<") || s.contains("&&") || s.contains("||"))
            return true;
        return false;
    }

    public boolean isArithmeticExp(String s){
        return s.matches("[a-zA-Z0-9-/\\+\\*\\(\\)]+") && (s.contains("+") || s.contains("-") || s.contains("*") || s.contains("/"));
    }

    public boolean isComparisonExp(String s){
        return s.matches("[a-zA-Z0-9><=]+") && (s.contains(">") || s.contains("<") || s.contains("="));
    }

    public int countStringInString(String s,String exp){
        int count = 0;
        int k = 0;
        while(k >= 0){
            k = exp.indexOf(s,k);
            if(k >= 0){
                k++;
                count++;
            }
        }
        return count;
    }

    public void print(){
        for(String s : map.keySet())
            System.out.println(s+": "+map.get(s));
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(AnnotationTypeMemberDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(ArrayAccess node) {

        return true;
    }

    @Override
    public boolean visit(ArrayCreation node) {
        String name = "array";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(ArrayInitializer node) {

        return true;
    }

    @Override
    public boolean visit(ArrayType node) {

        return true;
    }

    @Override
    public boolean visit(AssertStatement node) {

        return true;
    }

    @Override
    public boolean visit(Assignment node) {

        return true;
    }

    @Override
    public boolean visit(Block node) {
        //TODO DO THIS CRAP

        return true;
    }

    @Override
    public boolean visit(BlockComment node) {

        return true;
    }

    @Override
    public boolean visit(BooleanLiteral node) {

        return true;
    }

    @Override
    public boolean visit(BreakStatement node) {
        String name = "break_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(CastExpression node) {

        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
        String name = "catch_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(CharacterLiteral node) {

        return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {

        return true;
    }

    @Override
    public boolean visit(CompilationUnit node) {

        return true;
    }

    @Override
    public boolean visit(ConditionalExpression node) {

        return true;
    }

    @Override
    public boolean visit(ConstructorInvocation node) {

        return true;
    }

    @Override
    public boolean visit(ContinueStatement node) {

        return true;
    }

    @Override
    public boolean visit(DoStatement node) {

        return true;
    }

    @Override
    public boolean visit(EmptyStatement node) {

        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {

        return true;
    }

    @Override
    public boolean visit(EnumConstantDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(EnumDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(ExpressionStatement node) {

        return true;
    }

    @Override
    public boolean visit(FieldAccess node) {

        return true;
    }

    @Override
    public boolean visit(FieldDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        //Check for nested for loop
        if(hasThisParent(node,"ForStatement")){
            putInMap("nested_for_loop");
            putInMap(featureMap.get("nested_for_loop"));
        }

        //Check for loop starting at some variable
        String s = node.initializers().get(0).toString();
        String substr = s.substring(s.indexOf('=')+1).trim();
        if(!isInteger(substr)){
            putInMap("var_start_for_loop");
            putInMap(featureMap.get("var_start_for_loop"));
        } else if(Integer.parseInt(substr) > 0){
            putInMap("nonzero_start_for_loop");
            putInMap(featureMap.get("nonzero_start_for_loop"));
        }

        //Check if stopping condition uses <, <=, > or >=
        String exp = node.getExpression().toString().replaceAll(" ", "");
        if(exp.contains("<=")){
            putInMap("<=_stop_for_loop");
            putInMap(featureMap.get("<=_stop_for_loop"));
        } else if(exp.contains("<")){
            putInMap("<_stop_for_loop");
            putInMap(featureMap.get("<_stop_for_loop"));
        } else if(exp.contains(">=")){
            putInMap(">=_stop_for_loop");
            putInMap(featureMap.get(">=_stop_for_loop"));
        } else if(exp.contains(">")){
            putInMap(">_stop_for_loop");
            putInMap(featureMap.get(">_stop_for_loop"));
        }

        //Check if the stopping condition is at length/size or length/size -1
        if(exp.contains("length()-1") || exp.contains("length-1") || exp.contains("size()-1")){
            putInMap("len-1_stop_for_loop");
            putInMap(featureMap.get("len-1_stop_for_loop"));
        }else if(exp.contains("length()-") || exp.contains("length-") || exp.contains("size()-")){
            String x = exp.substring(exp.indexOf("-")+1);
            if(Character.isDigit(x.charAt(0))){
                putInMap("len-k_stop_for_loop");
                putInMap(featureMap.get("len-k_stop_for_loop"));
            } else {
                putInMap("len-var_stop_for_loop");
                putInMap(featureMap.get("len-var_stop_for_loop"));
            }
        } else if(exp.contains("length()") || exp.contains("length-1") || exp.contains("size()")){
            putInMap("len_stop_for_loop");
            putInMap(featureMap.get("len_stop_for_loop"));
        }

        String name = node.updaters().get(0).toString();
        if(name.length() > 2)
            name = name.substring(1, 3);
        name = name+"_for_loop";

        if(featureMap.get(name) != null){
            putInMap(name);
            putInMap(featureMap.get(name));
        }

        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        if(hasThisParent(node,"ForStatement")){
            putInMap("if_stmt_loop");
            putInMap(featureMap.get("if_stmt_loop"));
        } else if(hasThisParent(node,"WhileStatement")){
            putInMap("if_stmt_loop");
            putInMap(featureMap.get("if_stmt_loop"));
        }

        String myThen = node.getThenStatement().toString();
        String myElse = "";

        Statement tempNode = node.getElseStatement();
        String type = "null";
        int count = 0;
        if(tempNode != null)
            type = tempNode.getClass().getSimpleName();
        if(tempNode != null && type.equals("IfStatement")){
            type = tempNode.getClass().getSimpleName();
            while(tempNode != null && type.equals("IfStatement")){
                tempNode = ((IfStatement)tempNode).getElseStatement();
                if(tempNode != null)
                    type = tempNode.getClass().getSimpleName();
                else
                    type = "null";
                count++;
            }

            if(count == 1 && type.equals("null")){
                putInMap("if_else-if");
                putInMap(featureMap.get("if_else-if"));
                decInMap("if_no_else");
            } else if(count == 1 && !type.equals("null")){
                putInMap("if_else-if_else");
                putInMap(featureMap.get("if_else-if_else"));
            } else if(count == 2 && type.equals("null")){
                putInMap("if_else-if_else-if");
                putInMap(featureMap.get("if_else-if_else-if"));
                decInMap("if_else-if");
                decInMap("if_no_else");
            } else if(count == 2 && !type.equals("null")){
                putInMap("if_else-if_else-if_else");
                putInMap(featureMap.get("if_else-if_else-if_else"));
                decInMap("if_else-if_else");
            } else if(count > 2 && type.equals("null")){
                putInMap("if_k_else-if");
                putInMap(featureMap.get("if_k_else-if"));
                decInMap("if_else-if_else-if");
            } else if(count > 2 && !type.equals("null")){
                putInMap("if_k_else-if_else");
                putInMap(featureMap.get("if_k_else-if_else"));
                decInMap("if_else-if_else-if_else");

            }

        } else if(!type.equals("null")){
            putInMap("if_else");
            putInMap(featureMap.get("if_else"));
        } else {
            putInMap("if_no_else");
            putInMap(featureMap.get("if_no_else"));
        }

        if(getParent(node).equals("if")){
            putInMap("if_nested_in_then");
            putInMap(featureMap.get("if_nested_in_then"));
        }

        String exp = node.getExpression().toString().toLowerCase();
        exp = exp.replace(" ", "");

        int countAnd = countStringInString("&&",exp);
        int countOr = countStringInString("||",exp);

        if(countAnd == 1
                && countOr == 0
                && exp.matches("[a-zA-Z0-9>=<&]+")){
            putInMap("if_and_simple");
            putInMap(featureMap.get("if_and_simple"));
        }
        if(countOr == 1
                && countAnd == 0
                && exp.matches("[a-zA-Z0-9>=<\\|]+")){
            putInMap("if_or_simple");
            putInMap(featureMap.get("if_or_simple"));
        }

        if(exp.matches("[\\(][a-zA-Z0-9>=<&\\|\\+-]+[\\)][&]+[\\(][a-zA-Z0-9>=<&\\|\\+-]+[\\)]")){
            putInMap("if_and_complex");
            putInMap(featureMap.get("if_and_complex"));
        }
        if(exp.matches("[\\(][a-zA-Z0-9>=<&\\|\\+-]+[\\)][|]+[\\(][a-zA-Z0-9>=<&\\|\\+-]+[\\)]")){
            putInMap("if_or_complex");
            putInMap(featureMap.get("if_or_complex"));
        }


        if(node.getElseStatement() != null){
            myElse = node.getElseStatement().getClass().getSimpleName().toString();
        }

        if(!myThen.equals("Block") || (!myElse.equals(null) && !myElse.equals("Block"))){
            putInMap("no_block_if_stmt");
            putInMap(featureMap.get("no_block_if_stmt"));
        }

        String name = "if_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(ImportDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(InfixExpression node) {

        return true;
    }

    @Override
    public boolean visit(Initializer node) {

        return true;
    }

    @Override
    public boolean visit(InstanceofExpression node) {

        return true;
    }

    @Override
    public boolean visit(Javadoc node) {

        return true;
    }

    @Override
    public boolean visit(LabeledStatement node) {

        return true;
    }

    @Override
    public boolean visit(LineComment node) {

        return true;
    }

    @Override
    public boolean visit(MarkerAnnotation node) {

        return true;
    }

    @Override
    public boolean visit(MemberRef node) {

        return true;
    }

    @Override
    public boolean visit(MemberValuePair node) {

        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        String name = "method_declaration";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {

        return true;
    }

    @Override
    public boolean visit(MethodRef node) {

        return true;
    }

    @Override
    public boolean visit(MethodRefParameter node) {

        return true;
    }

    @Override
    public boolean visit(Modifier node) {

        return true;
    }

    @Override
    public boolean visit(NormalAnnotation node) {

        return true;
    }

    @Override
    public boolean visit(NullLiteral node) {

        return true;
    }

    @Override
    public boolean visit(NumberLiteral node) {

        return true;
    }

    @Override
    public boolean visit(PackageDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(ParameterizedType node) {

        return true;
    }

    @Override
    public boolean visit(ParenthesizedExpression node) {

        return true;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        String name = node.getOperator().toString()+"_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(PrefixExpression node) {

        return true;
    }

    @Override
    public boolean visit(PrimitiveType node) {

        return true;
    }

    @Override
    public boolean visit(QualifiedName node) {

        return true;
    }

    @Override
    public boolean visit(QualifiedType node) {

        return true;
    }

    @Override
    public boolean visit(ReturnStatement node) {    	

        if(hasThisParent(node,"ForStatement")){
            putInMap("return_stmt_loop");
            putInMap(featureMap.get("return_stmt_loop"));
            String temp = node.getExpression().toString().toLowerCase();
            if(temp.contains("true")){
                putInMap("return_true_in_loop");
                putInMap(featureMap.get("return_true_in_loop"));
            }
            if(temp.contains("false")){
                putInMap("return_false_in_loop");
                putInMap(featureMap.get("return_false_in_loop"));
            }
        } else if(hasThisParent(node,"WhileStatement")){
            putInMap("return_stmt_loop");
            putInMap(featureMap.get("return_stmt_loop"));
            String temp = node.getExpression().toString().toLowerCase();
            if(temp.contains("true")){
                putInMap("return_true_in_loop");
                putInMap(featureMap.get("return_true_in_loop"));
            }
            if(temp.contains("false")){
                putInMap("return_false_in_loop");
                putInMap(featureMap.get("return_false_in_loop"));
            }
        }

        String exp = "";
        if(node.getExpression() != null)
            exp = node.getExpression().toString().replace(" ", "");
        if(isArithmeticExp(exp)){
            putInMap("return_arithmetic");
            putInMap(featureMap.get("return_arithmetic"));
        }
        if(exp.matches("[a-zA-Z_]*") && !exp.contains("true") && !exp.contains("false")){
            putInMap("return_variable");
            putInMap(featureMap.get("return_variable"));
        }
        if(isInteger(exp) || exp.matches("\"(.)\"")){
            putInMap("return_literal");
            putInMap(featureMap.get("return_literal"));
        }
        if(isComparisonExp(exp)){
            putInMap("return_comparison");
            putInMap(featureMap.get("return_comparison"));
        }
        if(countStringInString("||",exp) > 1
                || countStringInString("&&",exp) > 1
                || (countStringInString("||",exp) >= 1 && countStringInString("&&",exp) >= 1)){
            putInMap("return_complex_logic");
            putInMap(featureMap.get("return_complex_logic"));
        } else {
            if(exp.contains("&&")&& countStringInString("&&",exp) == 1){
                putInMap("return_and");
                putInMap(featureMap.get("return_and"));
            }
            if(exp.contains("||")&& countStringInString("||",exp) == 1){
                putInMap("return_or");
                putInMap(featureMap.get("return_or"));
            }
        }
        if(exp.matches("[a-zA-Z0-9]+\\(\\)")){
            putInMap("return_function");
            putInMap(featureMap.get("return_function"));
        }

        String name = "return_stmt";
        putInMap(name);
        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(SimpleName node) {

        return true;
    }

    @Override
    public boolean visit(SimpleType node) {

        return true;
    }

    @Override
    public boolean visit(SingleMemberAnnotation node) {

        return true;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        String name = "parameter";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(StringLiteral node) {

        return true;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {

        return true;
    }

    @Override
    public boolean visit(SuperFieldAccess node) {

        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {

        return true;
    }

    @Override
    public boolean visit(SwitchCase node) {

        return true;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        String name = "switch_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(SynchronizedStatement node) {

        return true;
    }

    @Override
    public boolean visit(TagElement node) {

        return true;
    }

    @Override
    public boolean visit(TextElement node) {

        return true;
    }

    @Override
    public boolean visit(ThisExpression node) {

        return true;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        String name = "throw_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(TryStatement node) {
        String name = "try_stmt";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {

        return true;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {

        return true;
    }

    @Override
    public boolean visit(TypeLiteral node) {

        return true;
    }

    @Override
    public boolean visit(TypeParameter node) {

        return true;
    }

    @Override
    public boolean visit(UnionType node) {

        return true;
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {

        return true;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {

        return true;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        String name = "var_declaration";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        String name = "while_loop";
        putInMap(name);

        putInMap(featureMap.get(name));

        return true;
    }

    @Override
    public boolean visit(WildcardType node) {

        return true;
    }

}
