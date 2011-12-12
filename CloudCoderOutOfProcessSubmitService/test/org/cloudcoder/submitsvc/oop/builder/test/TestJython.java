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
    }
}
