package org.cloudcoder.analysis.features.java;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class Parse
{

    private static String readFile(String filename) 
    throws IOException
    {
        String res="";
        Scanner scan=new Scanner(new FileInputStream(filename));
        while (scan.hasNextLine()) {
            String line=scan.nextLine();
            res+=line+"\n";
        }
        return res;
    }
    /**
     * @param args
     */
    public static void main(String[] args) 
    throws Exception
    {
        String filePath="files/parser/MissingSemicolon.java";
        
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        String src = readFile(filePath);
        parser.setSource(src.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        
        cu.accept(new FeatureVisitor());
    }

}
