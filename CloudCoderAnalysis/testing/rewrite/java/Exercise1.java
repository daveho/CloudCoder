import java.io.FileOutputStream;
import java.io.PrintStream;

public class Exercise1 {
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
        getMax(new int[] {4, 5, 2, 6, 2, 3, 1, 7});
    }
}