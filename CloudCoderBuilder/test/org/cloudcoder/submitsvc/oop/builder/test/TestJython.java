package org.cloudcoder.submitsvc.oop.builder.test;

import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.python.core.PyCode;
import org.python.core.PyFunction;
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
    
    @Test
    public void testLegacyCompiler() throws Exception {
        //TODO: Strip out code that is not inside a function
        PythonInterpreter terp=new PythonInterpreter();
        terp.execfile(new FileInputStream("sq2.py"));
        PyFunction f=(PyFunction)terp.get("t1", PyFunction.class);
        PyObject r=f.__call__();
        System.out.println("Result is: "+r);

        //code.__call__
        //imp.compileSource("sq", new FileInputStream("sq2.py"), "sq.py");
        //PyCode pyc=Py.newJavaCode(PyTestTemplate.class, "t1");
        //code.__call__(pyc);
        //PyBuiltinFunction p=PyBuiltinFunction.asName()
        //__builtin__. __builtin__.
    }
}
