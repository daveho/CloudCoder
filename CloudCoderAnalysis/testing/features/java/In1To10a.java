public class Test {
    public static boolean in1to10(int n, boolean inside)
    {
         if(inside==true) {
         if(n<=10 && n>=1){
                return true;
         } else {
                return false;
         }
         } else{
             if(n>10 || n<1){
                    return true;
             } else {
                    return false;
             }
            
         }
    }
}