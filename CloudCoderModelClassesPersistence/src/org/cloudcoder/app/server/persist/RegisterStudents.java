package org.cloudcoder.app.server.persist;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.IFactory;
import org.cloudcoder.app.shared.model.Term;

public class RegisterStudents
{
    public static void main(String[] args) {
        try {
            registerStudents();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * @param args
     */
    private static void registerStudents() throws Exception
    {
        Scanner keyboard=new Scanner(System.in);
        Class.forName("com.mysql.jdbc.Driver");
        Properties config = DBUtil.getConfigProperties();
        Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");

        List<Course> courses = DBUtil.getAllModelObjects(conn, Course.SCHEMA, new IFactory<Course>() {
            @Override
            public Course create() {
                return new Course();
            }
        });
        Course c=CreateCourse.choose(keyboard, "For which course would you like to register students?", courses);
        // TODO: look up the term for each course
        String filename=CreateWebappDatabase.ask(keyboard, "Enter the name of the file containing a tab-separated list of usernames and passwords");
        int num=registerStudentsForCourseId(new FileInputStream(filename), c.getId(), conn);
        System.out.println("Registered "+num+" students for "+c.getName());
    }

    public static int registerStudentsForCourseId(InputStream in, int courseId, Connection conn) throws SQLException
    {
        Scanner scan=new Scanner(in);
        int num=0;
        while (scan.hasNextLine()) {
            String line=scan.nextLine().replaceAll("#.*","").trim();
            if (line.equals("")) {
                continue;
            }
            String[] tokens=line.split("\t");
            String username=tokens[0];
            String password=tokens[1];
            // Look up the user to see if they already exist
            int userId=lookupUser(conn, username);
            if (userId==-1) {
                // user doesn't already exist, so look the user up
                userId=CreateSampleData.createInitialUser(conn, username, password);
            }
            // TODO check that user is not already registered
            CreateSampleData.registerUser(conn, userId, courseId, CourseRegistrationType.STUDENT);
            num++;
        }
        return num;
    }
    
    public static int lookupUser(Connection conn, String username)
    throws SQLException
    {
        return -1;
    }

}
