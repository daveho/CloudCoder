import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;


public class Exercise1
{
    
    static int fooLocals(int param) {
        int x=10;
        return x+param;
    }

    static int getMax(int[] arr) {
        int max=arr[0];
        for (int i=0; i<arr.length; i++) {
            if (arr[i]>max){
                max=arr[i];
            }
        }
        return max;
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
