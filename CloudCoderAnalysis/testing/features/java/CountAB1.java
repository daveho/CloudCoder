public class Test {
    public static int countAB(String str)
    {
        //int[] arr;
         // code goes here
         int count=0;
         for (int i=0; i<str.length()-1; i++) {
             if (str.charAt(i)=='a'&&str.charAt(i+1)=='b') {
                    count++;
             }
         }
         return count;
    }    
}