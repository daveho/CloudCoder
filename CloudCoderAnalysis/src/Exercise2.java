import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

//import experimental.IVariableStore;


public class Exercise2
{
    
    /*
     * For each line, we must know the values of all the vars there
     * Possible values:
     * undef (not yet defined)
     * null (legitimate null)
     * actual value (as a String for easy printing; use .equals() to compare)
     *      Only allow primitive types, Strings, and arrays of ints or Strings
     *      
     * for a loop, we need a separate loop iteration counter at the top of the loop
     * 
     * So we need to map line numbers to a map from variables to their values.
     * 
     * Of course there may be a nested loop, so might need to map from line nums
     * to loop iterations, and from loop iterations to values.
     * 
     * What about a class called "Scope", which encapsulates 
     */
    
    static int ___currentLineNum=0;
    static int ___loop01iter=0;
    //static Map<Integer, IVariableStore> ___lineMap=new TreeMap<Integer,IVariableStore>();
    
    

    static int getMax(int[] arr) {
        // Line 1 (values of the arr)
        ___currentLineNum=1;
        int max=arr[0];
        // Line 2 (max)
        ___currentLineNum=2;
        int ___loop1=0;
        for (int i=0; i<arr.length; i++) {
            
            // Line 3 (loop header)
            ___currentLineNum=3;
            ___loop1++;
            
            if (arr[i]>max){
                // Line 4
                ___currentLineNum=4;
                max=arr[i];
                // Line 5
            }
            // Line 6
        }
        // Line 7
        ___currentLineNum=7;
        return max;
        // Line 8
    }
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception 
    {
        System.out.println("OUTPUT DUDE");
        PrintStream out=new PrintStream(new FileOutputStream("/tmp/FOOBAR"));
        out.println("Does this effin work?");
        out.flush();
        out.close();
        //Scanner scan=new Scanner(System.in);
        //scan.next();
        getMax(new int[] {4, 5, 2, 6, 2, 3, 1, 7});
    }

}
