package org.cloudcoder.app.server.persist;

public class PrintHelp
{
    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Command-line configuration options:");
        System.out.println("    createdb  (Create a fresh cloudcoder database using the current configuration settings " +
                "\n       in the jarfile that you are currently executing.  This should only be" +
                "\n       be done once!)");
        System.out.println("    migratedb  (Update the currently existing database with any new tables or columns)");
        System.out.println("    createuser  (Create a new user account in database of the CloudCoder installation)");
        System.out.println("    createcourse  (Create a new course in the database of the CloudCoder installation)");
        System.out.println("    configure  (Set new configuration parameters in the CloudCoder jarfile.  Parameters " +
        		"\n     be read interactively or can be set in a properties file, usually named cloudcoder.properties)");
        System.out.println("    registerstudents  (Register students defined in a a tab-delimited text file)");
        System.out.println("    listconfig  (Lists configuration parameters set in the current CloudCoder jarfile)");
        System.out.println("    help  (Prints this help message)");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("To start the CloudCoder server:");
        System.out.println("start");
        System.out.println("To stop/shutdown the CloudCoder server:");
        System.out.println("shutdown");
    }
    

}
