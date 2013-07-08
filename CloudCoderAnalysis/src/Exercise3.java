import org.cloudcoder.analysis.tracing.rewriter.java.CodePath;
import org.cloudcoder.analysis.tracing.rewriter.java.VariableStore;


public class Exercise3
{
    /*
     * arr exists at all lines
     * max exists after line #1
     * i exists between lines 2 and 6
     */
    
    //static int ___currentLineNum=0;
    static int ___loop1=0;
    static CodePath ___varchain=new CodePath();
    
    static int getMax(int[] arr) {
        // These are the variables for tracking things
        
        // Line 0 (parameters)
        ___varchain.startNewLine(0, ___loop1);
        ___varchain.addVariable("arr", arr);
        ___varchain.endLine();
        
        int max=arr[0];
        // Line 1 (values of the arr)
        
        ___varchain.startNewLine(1, ___loop1);
        ___varchain.addVariable("max", max);
        ___varchain.addVariable("arr", max);
        ___varchain.endLine();
        
        ___loop1=0;
        for (int i=0; i<arr.length; i++) {
            // Line 2 (max)
            ___loop1++;
            ___varchain.startNewLine(2, ___loop1);
            ___varchain.addVariable("max", max);
            ___varchain.addVariable("i", i);
            ___varchain.addVariable("arr", arr);
            ___varchain.endLine();
            
            if (arr[i]>max){
                // Line 3
                ___varchain.startNewLine(3, ___loop1);
                ___varchain.addVariable("max", max);
                ___varchain.addVariable("i", i);
                ___varchain.addVariable("arr", arr);
                ___varchain.endLine();
                
                max=arr[i];
                // Line 4
                ___varchain.startNewLine(3, ___loop1);
                ___varchain.addVariable("max", max);
                ___varchain.addVariable("i", i);
                ___varchain.addVariable("arr", arr);
                ___varchain.endLine();
                // Line 5
                // TODO: store empty parts of statements
                // such as closing brackets
            }
            // Line 6
        }
        ___loop1=0;

        // Line 7
        ___varchain.startNewLine(7, ___loop1);
        ___varchain.addVariable("max", max);
        ___varchain.addVariable("arr", arr);
        ___varchain.endLine();
        //TODO How to handle return statements?
        // Probably need to re-write the code.  Blech.
        return max;
        // Line 8
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception 
    {
        getMax(new int[] {4, 5, 2, 6, 2, 3, 1, 7});
        
        System.out.println(___varchain);
        
    }

}
