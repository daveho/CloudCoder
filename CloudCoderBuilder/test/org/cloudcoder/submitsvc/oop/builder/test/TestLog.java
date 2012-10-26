package org.cloudcoder.submitsvc.oop.builder.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLog
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // This masks a crapload of configuration tinkering to get logging to work
        Logger log=LoggerFactory.getLogger(TestLog.class);
        log.info("Hello dude");
        
    }

}
