public class VarChecker {
    
    int x;
    
    static int y;
    
    static {
        System.out.println("static initializers are weird");
    }
    
    static int foo() {
        System.out.println("Foo");
        return 0;
    }
    
    static int max(int[] arr1, int[] arr2, 
        int x, int y, String z)
    {
        for (int i=0, j=0; i<arr1.length; i++) {
            int max=123;
            System.out.println(arr1[i]+arr2[j]);
        }
        int local1=5;
        String str;
        
        for (int i=0; i<10; i++) {
            for (int j=0; j<=i; j++) {
                local1++;
            }
        }
        
        {
            int strangeInnerBlockLocal=9;
        }
        String s="this is a really really really really really " +
        "really really really long String";
        if (s.equals("")) {
            local1=10;
        }
        else 
        {
            local1=-1;
        }
                
        while(count<10) {
            int c2=count+local1;
            System.out.println(c2);
            count++;
            if (count>2&&local1>0) {
                break;
            }
        }
        
        return 0;
    }
}