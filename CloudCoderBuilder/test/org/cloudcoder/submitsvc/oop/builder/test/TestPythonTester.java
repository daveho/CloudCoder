package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.submitsvc.oop.builder.PythonTester;
import org.junit.Before;

public class TestPythonTester extends TestSq
{
    
    @Before
    public void before() {
        createProblem("sq", ProblemType.PYTHON_FUNCTION);
        
        tester=new PythonTester();
        setProgramText("def sq(x):\n" +
                "  if x==1:\n" +
                "    return 17\n" +
                "  if x==2:\n" +
                "    raise Exception('error')\n"+
                "  if x==3:\n"+
                "    while True:\n" +
                "      pass\n" +
                "  if x==4:\n" +
                "    from subprocess import call\n" +
                "    call(['ls', '-l'])\n" +
                "  if x==5:\n" +
                "    return x*x\n"+
                "  if x==6:\n" +
                "    import socket\n" +
                "    sock = socket.socket()\n" +
                "    sock.connect((localhost, 8888))\n" +
                "  return x*x\n");
    }
}
