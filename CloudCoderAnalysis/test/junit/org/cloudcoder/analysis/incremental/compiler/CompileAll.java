package junit.org.cloudcoder.analysis.incremental.compiler;

import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.analysis.incremental.compiler.EditSequence;
import org.cloudcoder.analysis.incremental.compiler.LineEdit;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;
import org.cloudcoder.app.shared.model.ApplyChangeToTextDocument;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TextDocument;
import org.cloudcoder.app.shared.model.User;
import org.junit.Test;

import static org.junit.Assert.*;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


public class CompileAll
{
    /*
     * 
     * Strategies:
     *      Compile every change at whatever granularity
     *      
     *      Compile every line (i.e. look-ahead to see 
     *          when we start editing a new line number)
     *      
     *      Reconstruct token sequence, compile token-by-token
     *      
     *      Some other choice of granularity?
     *      
     * 
     * Future work: use the Eclipse incremental compiler
     * 
     * Take the change sequence and reconstruct
     * tokens.  Probably have to find the spaces.
     * May want to compile after each edit to a line,
     * or work on consecutive edits within a line.
     * 
     * May need to recognize when an edit is part of the same token
     * and when it is a new token (i.e. state machine that can
     * look ahead by characters).
     * 
     * How to handle edits that are finer or courser than an edit?
     * i.e. what if an edit contains several tokens, or a single character
     * that doesn't make sense without the next character?  We can't key these
     * edits to the eventID.
     * 
     * Maybe use a BufferedReader to read from
     * a StringReader, and have a lookup of some kind
     * to find the location for the token to be added
     * or removed.
     * 
     * What format do we want? 
     */
    
    static {
        create();
    }
    
    public static void create() {
        // XXX this is a hack!
        // TODO figure out how to put this info into a config file
        JDBCDatabaseConfig.create(new JDBCDatabaseConfig.ConfigProperties() {
            @Override
            public String getUser() {
                return "root";
            }
            @Override
            public String getPasswd() {
                return "root";
            }
            @Override
            public String getDatabaseName() {
                return "cloudcoderdb_andrei";
            }
            @Override
            public String getHost() {
                return "localhost";
            }
            @Override
            public String getPortStr() {
                return ":8889";
            }
        });
    }
    
    static List<String> convert(String lines) {
        lines=lines.replaceAll("\r", "");
        List<String> result=new LinkedList<String>();
        for (String s : lines.split("\n")) {
            result.add(s);
        }
        return result;
    }
    
    @Test
    public void printLineByLine()
    throws Exception
    {
        Integer userID=70;
        Integer problemID=16;
        List<Change> changeList=lookupChanges(userID, problemID);
        EditSequence lineChanges=new EditSequence();
        lineChanges.parseChanges(changeList);
        
        TextDocument doc=new TextDocument();
        doc.setText("");
        
        ApplyChangeToTextDocument applicator=new ApplyChangeToTextDocument();
        
        for (LineEdit line : lineChanges) {
            // this variable is useless and exists to hold a breakpoint
            int x=0;
            for (Change c : line) {
                applicator.apply(c, doc);
            }
            System.out.println(line);
            System.out.println(doc.getText());
        }
    }
    
    @Test
    public void testReconstructLineLevelEdits()
    throws Exception
    {
        Integer userID=70;
        Integer problemID=16;
        List<Change> changeList=lookupChanges(userID, problemID);
        EditSequence lineChanges=new EditSequence();
        lineChanges.parseChanges(changeList);
        assertEquals(changeList.size(), lineChanges.getNumChanges());
        for (int i=0; i<changeList.size(); i++) {
            Change c1=changeList.get(i);
            Change c2=lineChanges.getAllChanges().get(i);
            assertEquals(c1, c2);
        }
        for (LineEdit ch : lineChanges) {
            System.out.println(ch);
        }
    }
    
    static List<Change> lookupChanges(Integer userID, Integer problemID) {
        // look up the problem
        Problem problem=Database.getInstance().getProblem(problemID);
        Language lang=problem.getProblemType().getLanguage();
        
        // Get all of the changes for this problem
        // We need a fake user object here to match the API
        // of the getAllChangesNewerThan() method
        User user=new User();
        user.setId(userID);
        return Database.getInstance().getAllChangesNewerThan(user, problemID, -1);
    }
    
    @Test
    public void testPrintChanges()
    throws Exception
    {
        
        Integer userID=70;
        Integer problemID=16;
        
        
        List<Change> deltaList = lookupChanges(userID, problemID);
        
        ApplyChangeToTextDocument app=new ApplyChangeToTextDocument();
        TextDocument doc=new TextDocument();
        doc.setText("");
        System.out.println(doc);
        
        int i=1;
        int numRemove=0;
        int different=0;
        System.out.println(doc.getText());
        for (Change c : deltaList) {
            //if (i>=30) break;
            String before=doc.getText();
            app.apply(c, doc);
            String after=doc.getText();
            System.out.println(i+"\n"+c+"\nvvvvvvvvvvv after change vvvvvvvvvv");
            System.out.println(doc.getText());
            System.out.println("-------------------");
            if (c.getType()==ChangeType.FULL_TEXT && !before.equals(after)) {
                Patch p=DiffUtils.diff(convert(before), convert(after));
                if (p.getDeltas().size()==0) {
                    continue;
                }
                System.out.println("before is not the same as after:");
                for (Delta d: p.getDeltas()) {
                    System.out.println(d);
                }
                
                different++;
                System.out.println("-------------------");
            }
            
            
            if (c.getType()==ChangeType.REMOVE_TEXT) {
                numRemove++;
            }
            i++;
        }
        System.out.println("Num remove text: "+numRemove);
        System.out.println("Num different: "+different);
        
    }
    
    
//    public boolean compile() {
//        InMemoryJavaCompiler compiler=new InMemoryJavaCompiler();
//        compiler.addSourceFile()
//    }
}
