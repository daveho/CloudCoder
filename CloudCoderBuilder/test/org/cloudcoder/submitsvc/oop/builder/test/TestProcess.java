package org.cloudcoder.submitsvc.oop.builder.test;

import java.io.File;
import java.io.IOException;

public class TestProcess
{

    /**
     * @param args
     */
    public static void main(String[] args) 
    throws IOException, InterruptedException
    {
        System.out.println(System.getenv("PATH"));
        File dir=new File("/Users/jaimespacco/projects/git/CloudCoder/CloudCoderOutOfProcessSubmitService/builder");
        Process p=Runtime.getRuntime().exec(
                new String[] {dir+File.separator+"program", "test1"},
                //new String[] {"program", "test1"},
                new String[] {"PATH="+System.getenv("PATH")+":."},
                dir
                );
        p.waitFor();

    }

}
