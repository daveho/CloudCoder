package org.cloudcoder.app.server.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ConfigureCloudCoder
{
    private static final String YES = "yes";

    public static void main(String[] args) throws Exception {
        try {
            configureCloudCoder();
        } catch (SQLException e) {
            // Handle SQLException by printing an error message:
            // these are likely to be meaningful to the user
            // (for example, can't create the database because
            // it already exists.)
            e.printStackTrace(System.err);
            System.err.println(e.getMessage());
        }
    }
    private static void configureCloudCoder()
    throws Exception
    {
        // re-write configuration properties
        Scanner keyboard=new Scanner(System.in);
        String readAppFromFile=ConfigurationUtil.ask(keyboard, "Do you want to read new configuration properties from a file and add them to your webapp and/or builder?","no");
        
        if (readAppFromFile.equalsIgnoreCase(YES)) {
            String filename=ConfigurationUtil.ask(keyboard, "What is the name of the file containing the new configuration properties?", "cloudcoder.properties");
            Properties properties = new Properties();
            properties.load(new FileInputStream(filename));
            
            String configWebapp=ConfigurationUtil.ask(keyboard, "Do you want to set these configuration properties for your CloudCoder webapp?", YES);
            if (configWebapp.equals(YES)) {
                String webappJarfileName=ConfigurationUtil.ask(keyboard, "What is the name of the jarfile containing the code for the CloudCoder webapp?", "cloudcoderApp.jar");
                copyJarfileWithNewProperties(webappJarfileName, "cloudcoder.properties", properties);
                System.out.println("Wrote new configuration properties to cloudcoder.properties contained in jarfile "+webappJarfileName);
            }

            String configBuilder=ConfigurationUtil.ask(keyboard, "Would you like to set these configuration properties for your CloudCoder builder?",YES);
            if (configBuilder.equals(YES)) {
                String buildJarfileName=ConfigurationUtil.ask(keyboard, "What is the name of the jarfile containing the code for the CloudCoder builder?", "cloudcoderBuilder.jar");
                copyJarfileWithNewProperties(buildJarfileName, "cloudcoder.properties", properties);
                System.out.println("Wrote new configuration properties to cloudcoder.properties contained in jarfile "+buildJarfileName);
            }
        } else {
            System.out.println("OK, Nothing to do.  Goodbye!");
        }
    }
    
    /**
     * copy input to output stream - available in several StreamUtils or Streams classes 
     */    
    private static void copy(InputStream input, OutputStream output)
    throws IOException
    {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER))!= -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }
    private static final byte[] BUFFER = new byte[4096 * 1024];

    private static void copyJarfileWithNewProperties(String jarfileName, 
        String propertiesFileName,
        Properties newProps)
    throws Exception
    {
        // read in jarfileName, and replace propertiesFileName with newProps
        ZipFile jarfile = new ZipFile(jarfileName);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        ZipOutputStream newJarfileData = new ZipOutputStream(bytes);

        // first, copy contents from existing war
        Enumeration<? extends ZipEntry> entries = jarfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            //System.out.println("copy: " + e.getName());
            
            if (e.getName().equals(propertiesFileName)) {
                // If we find the file we're interested in, copy it!
                ZipEntry newEntry = new ZipEntry(propertiesFileName);
                newJarfileData.putNextEntry(newEntry);
                newProps.store(newJarfileData, "");
            } else {
                newJarfileData.putNextEntry(e);
                if (!e.isDirectory()) {
                    copy(jarfile.getInputStream(e), newJarfileData);
                }
            }
            newJarfileData.closeEntry();
        }

        // close
        newJarfileData.close();
        bytes.flush();
        bytes.close();
        jarfile.close();
        
        // copy over the file with new version we had just changed
        FileOutputStream out=new FileOutputStream(jarfileName);
        ByteArrayInputStream in=new ByteArrayInputStream(bytes.toByteArray());
        copy(in, out);
        
        out.close();
    }
}
