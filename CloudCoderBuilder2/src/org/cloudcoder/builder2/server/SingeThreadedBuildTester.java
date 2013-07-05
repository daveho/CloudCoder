package org.cloudcoder.builder2.server;

import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.builder2.javasandbox.JVMKillableTaskManager;
import org.cloudcoder.builder2.server.Builder2Daemon.Options;
import org.cloudcoder.daemon.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingeThreadedBuildTester
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 
     */
    public static void main(String[] args) {
        SingeThreadedBuildTester tester=new SingeThreadedBuildTester();
        tester.start();
    }
    
    public void start() {
     // If embedded configuration properties exist, read them
        Properties config;
        try {
            String configPropPath = "testing.properties";
            ClassLoader clsLoader = this.getClass().getClassLoader();
            config = Util.loadPropertiesFromResource(clsLoader, configPropPath);
        } catch (IllegalStateException e) {
            logger.warn("Could not load cloudcoder.properties, using default config properties");
            config = new Properties();
        }
        
        Options options = new Options(config);
        
        // Create the WebappSocketFactory which the builder tasks can use to create
        // connections to the webapp.
        WebappSocketFactory webappSocketFactory;
        try {
            webappSocketFactory = new WebappSocketFactory(
                    options.getAppHost(),
                    options.getAppPort(),
                    options.getKeystoreFilename(),
                    options.getKeystorePassword());
        } catch (Exception e) {
            logger.error("Could not create WebappSocketFactory", e);
            throw new IllegalStateException("Could not create WebappSocketFactory", e);
        }
        
        // Install KillableTaskManager's security manager
        JVMKillableTaskManager.installSecurityManager();
        
        logger.info("Builder starting");
        logger.info("appHost={}", options.getAppHost());
        logger.info("appPort={}", options.getAppPort());
        logger.info("numThreads={}", options.getNumThreads());

        Builder2Server builder=new Builder2Server(webappSocketFactory, config);
        
        Scanner scan=new Scanner(System.in);
        while(true) {
            builder.runOnce();
            System.out.println("type 'quit' to stop or anything else to run the builder again.");
            String s=scan.next();
            if (s.equals("quit")) {
                break;
            }
        }
        
    }

}
