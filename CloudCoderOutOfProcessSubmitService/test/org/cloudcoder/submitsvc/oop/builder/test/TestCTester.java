package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.submitsvc.oop.builder.CTester;
import org.junit.Before;

public class TestCTester extends TestSq
{
    @Before
    public void before() {
        createProblem("sq", ProblemType.JAVA_METHOD);
        
        tester=new CTester();
        
        setProgramText("#include <stdlib.h>\n"+
                "#include <sys/types.h>\n"+
                "#include <sys/socket.h>\n"+
                "int sq(int x) { \n" +
                " int * crash=NULL; \n" +
                " if (x==1) return 17; \n" +
                " if (x==2) *crash=1; \n" +
                " if (x==3) while (1); \n" +
                " if (x==4) system(\"/bin/ls\");\n" + //currently cannot block illegal operations
                " if (x==5) return x*x; \n" + // correct
                " if (x==6) x = socket(AF_INET, SOCK_STREAM, 0);\n" +//currently cannot block illegal operations
                " return x*x; \n" +
                    "}");
    }
    
}
