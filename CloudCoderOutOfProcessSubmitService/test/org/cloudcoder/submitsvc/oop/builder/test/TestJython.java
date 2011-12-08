package org.cloudcoder.submitsvc.oop.builder.test;

import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class TestJython
{
    @Test
    public void testJython() throws Exception {
        PythonInterpreter terp=new PythonInterpreter();
        String s=IOUtils.toString(new FileInputStream("sq2.py"));
        PyCode code1=terp.compile(s, "sq2");
        terp.eval(code1);
        PyObject True=terp.eval("True");
        PyObject False=terp.eval("False");
        
        System.out.println(True==terp.eval("Tester().test1()"));
        System.out.println(False==terp.eval("Tester().test2()"));
        
        //System.out.println(terp.eval("Tester().test1()"));
        
    }

//    private static String executeFunction(String body, 
//            PythonInterpreter terp, 
//            String input)
//    {
//        terp.exec("theRes=sq("+input+")");
//        return terp.get("theRes").toString();
//    }
//
//    @Test
//    public void testInterpreter() throws Exception {
//        PythonInterpreter terp=new PythonInterpreter();
//        assertEquals("16", executeFunction(terp, "4"));
//        //PyCode pyCode=terp.compile(body);
//        //pyCode.invoke("sq", new PyObject[] {new PyOb"4"});
//        terp.exec(body);
//        terp.exec("res=sq(4)");
//        PyObject res=terp.get("res");
//        //System.out.println("type: "+res.getType());
//        //System.out.println("class: "+res.getClass());
//        //res._eq()
//        assertEquals("16", res.toString());
//        System.out.println(res);
//    }

}
