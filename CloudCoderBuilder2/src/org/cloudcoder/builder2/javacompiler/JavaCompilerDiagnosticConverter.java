// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.builder2.javacompiler;

import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilerDiagnostic;

/**
 * Converts Compiler
 * 
 * @author jaimespacco
 *
 */
public class JavaCompilerDiagnosticConverter
{
    private static void adjustMessage(CompilerDiagnostic diagnostic) {
        String message=diagnostic.getMessage();
        message=message.replaceAll("mfm:///Test.java:[0-9]+:\\s+", "");
        message=message.replaceAll("location: class Test", "");
        diagnostic.setMessage(message);
    }
    
    private static boolean isMissingSemicolon(CompilerDiagnostic diagnostic) {
        return diagnostic.getMessage().contains("';' expected");
    }
    
    private static boolean isIllegalStartOfType(CompilerDiagnostic diagnostic) {
        return diagnostic.getMessage().contains("illegal start of type");
    }
    
    private static boolean isReachedEndOfFileWhileParsing(CompilerDiagnostic diagnostic) {
        return diagnostic.getMessage().contains("reached end of file while parsing");
    }
    
    private static boolean isMissingRightParen(CompilerDiagnostic diagnostic) {
        return diagnostic.getMessage().contains("')' expected");
    }
    
    private static boolean isMissingReturnStatement(CompilerDiagnostic diagnostic) {
        return diagnostic.getMessage().contains("missing return statement");
    }
    
    /**
     * Given an array of compiler diagnostics, convert/refactor/clean up
     * the messages to make them more readable or understandable.
     * This method may even cut down the number of 
     * 
     * @param diagnosticList
     * @return
     */
    public CompilerDiagnostic[] convertCompilerDiagnostics(CompilerDiagnostic[] diagnosticList)
    {
        CompilerDiagnostic[] newDiagnosticList=new CompilerDiagnostic[diagnosticList.length];
        for (int i=0; i<diagnosticList.length; i++) {
            newDiagnosticList[i]=convertCompilerDiagnostic(diagnosticList[i]);
        }
        return newDiagnosticList;
    }
    
    /**
     * Only report the first error message.
     * 
     * @param diagnosticList
     * @return a new list of compiler errors.
     */
    public List<CompilerDiagnostic> convertCompilerDiagnostics(List<CompilerDiagnostic> diagnosticList)
    {
        List<CompilerDiagnostic> newDiagnosticList=new LinkedList<CompilerDiagnostic>();
        for (CompilerDiagnostic d : diagnosticList) {
            newDiagnosticList.add(convertCompilerDiagnostic(d));
            // Any errors we see after one of these errors are probably not helpful
            if (isIllegalStartOfType(d)) {
                break;
            }
            if (isReachedEndOfFileWhileParsing(d)) {
                break;
            }
            if (isMissingRightParen(d)) {
                break;
            }
            if (isMissingReturnStatement(d)) {
                break;
            }
        }
        return newDiagnosticList;
    }
    
    private static CompilerDiagnostic convertCompilerDiagnostic(CompilerDiagnostic d)
    {
        d=new CompilerDiagnostic(d);
        // Remove the mfm://Test.java part from the message
        adjustMessage(d);
        if (isMissingSemicolon(d)) {
            // missing semicolon errors always flag the line AFTER the missing semicolon
            modifyLineNumbers(d, -1);
        } else if (isIllegalStartOfType(d)) {
            // "illegal start of type" usually means there's a missing open bracket 
            d.setMessage(d.getMessage()+": \nOften this message means that you are missing a { BEFORE this line");
        } else if (isReachedEndOfFileWhileParsing(d)) {
            d.setMessage(d.getMessage()+": \nOften this means that you are missing a } AT OR NEAR THE END of the program");
        } else if (isMissingRightParen(d)) {
            d.setMessage(d.getMessage()+": \nIf you are concatenating Strings, check for a missing + sign");
            modifyLineNumbers(d, -1);
        } else if (isMissingReturnStatement(d)) {
            d.setMessage(d.getMessage()+" \nMake sure there is a return statement on EVERY path");
        }
        return d;
    }
    
    private static void modifyLineNumbers(CompilerDiagnostic d, int num) {
        d.setStartLine(d.getStartLine()+num);
        d.setEndLine(d.getEndLine()+num);
    }
}
