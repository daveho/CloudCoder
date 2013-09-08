public class Test {
    public static int countA(String str)
    {
        //int[] arr;
         // code goes here
         int count=0;
         for (int i=0; i<str.length(); i++) {
             if (str.charAt(i)=='a') {
                    count++;
             }
         }
         return count;
    }    
}