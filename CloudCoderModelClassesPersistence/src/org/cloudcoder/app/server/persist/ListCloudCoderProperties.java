package org.cloudcoder.app.server.persist;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.cloudcoder.daemon.Util;

public class ListCloudCoderProperties
{
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        String filename="cloudcoder.properties";
        Properties ccProps=Util.loadPropertiesFromResource(ListCloudCoderProperties.class.getClassLoader(), filename);
        // go through properties in a certain order
        List<String> keys=Arrays.asList("gwt.sdk",
                "cloudcoder.db.user",
                "cloudcoder.db.passwd",
                "cloudcoder.db.databaseName",
                "cloudcoder.db.host",
                "cloudcoder.db.portStr",
                "cloudcoder.login.service",
                "cloudcoder.submitsvc.oop.host",
                "cloudcoder.submitsvc.oop.numThreads",
                "cloudcoder.submitsvc.oop.port",
                "cloudcoder.submitsvc.ssl.cn",
                "cloudcoder.submitsvc.ssl.keystore",
                "cloudcoder.submitsvc.ssl.keystore.password",
                "cloudcoder.webserver.port",
                "cloudcoder.webserver.contextpath",
                "cloudcoder.webserver.localhostonly");
        for (String key : keys) {
            if (ccProps.containsKey(key)) {
                System.out.println(key+"="+ccProps.getProperty(key));
            }
        }
        for (Entry e : ccProps.entrySet()) {
            //OK, fine, this requires a few linear scans
            if (!keys.contains(e.getKey())) {
                System.out.println(e.getKey()+"="+e.getValue());
            }
        }
    }

}
