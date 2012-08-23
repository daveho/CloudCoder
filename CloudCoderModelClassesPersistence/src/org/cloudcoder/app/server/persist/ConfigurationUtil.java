package org.cloudcoder.app.server.persist;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.User;

public class ConfigurationUtil
{

    static String ask(Scanner keyboard, String prompt) {
        return ConfigurationUtil.ask(keyboard, prompt, null);
    }

    static<E> E choose(Scanner keyboard, String prompt, List<E> values) {
        System.out.println(prompt);
        int count = 0;
        for (E val : values) {
            System.out.println((count++) + " - " + val);
        }
        System.out.print("[Enter value in range 0.." + (values.size()-1) + "] ");
        int choice = Integer.parseInt(keyboard.nextLine().trim());
        return values.get(choice);
    }

    static int askInt(Scanner keyboard, String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(keyboard.nextLine().trim());
    }

    static String askString(Scanner keyboard, String prompt) {
        System.out.print(prompt);
        return keyboard.nextLine();
    }

    static CourseRegistration findRegistration(Connection conn, int userId, int courseId) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = conn.prepareStatement("select * from " + CourseRegistration.SCHEMA.getDbTableName() + " where user_id = ? and course_id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            resultSet= stmt.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            CourseRegistration reg=new CourseRegistration();
            DBUtil.loadModelObjectFields(reg, CourseRegistration.SCHEMA, resultSet);
            return reg;

        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }
    }
    
    static User findUser(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = conn.prepareStatement("select * from " + User.SCHEMA.getDbTableName() + " where username = ?");
            stmt.setString(1, username);

            resultSet= stmt.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            User user = new User();
            DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
            return user;

        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }
    }
    
    /**
     * Create a user.
     * 
     * @param conn        database connection
     * @param ccUserName  user name
     * @param ccPassword  password (plaintext)
     * @return the user id of the newly-created user
     * @throws SQLException
     */
    public static int createUser(Connection conn, 
            String ccUserName, 
            String firstname, 
            String lastname, 
            String email, 
            String ccPassword) 
    throws SQLException
    {
        User user = new User();
        user.setUsername(ccUserName);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(ccPassword, BCrypt.gensalt(12)));
        DBUtil.storeModelObject(conn, user);
        return user.getId();
    }

    static String ask(Scanner keyboard, String prompt, String defval) {
    	System.out.println(prompt);
    	System.out.print("[default: " + (defval != null ? defval : "") + "] ==> ");
    	String value = keyboard.nextLine();
    	if (value.trim().equals("") && defval != null) {
    		value = defval;
    	}
    	return value;
    }

    public static int registerStudentsForCourseId(InputStream in, int courseId, Connection conn) throws SQLException
    {
        Scanner scan=new Scanner(in);
        int num=0;
        int skip=0;
        while (scan.hasNextLine()) {
            String line=scan.nextLine().replaceAll("#.*","").trim();
            if (line.equals("")) {
                continue;
            }
            String[] tokens=line.split("\t");
            String username=tokens[0];
            String firstname=tokens[1];
            String lastname=tokens[2];
            String email=tokens[3];
            String password=tokens[4];
            int section = 101; // The default section number
            if (tokens[5] != null) {
            	section=Integer.parseInt(tokens[5]);
            }
            // Look up the user to see if they already exist
            int userId;
            User u=findUser(conn, username);
            if (u!=null) {
                userId=u.getId();
            } else {
                // user doesn't already exist, so create a new one
                userId=createUser(conn, 
                        username,
                        firstname,
                        lastname,
                        email,
                        password);
            }
            // TODO check that user is not already registered
            CourseRegistration reg=findRegistration(conn, userId, courseId);
            if (reg==null) {
                CreateSampleData.registerUser(conn, userId, courseId, CourseRegistrationType.STUDENT, section);
                num++;
            } else {
                skip++;
            }
        }
        return num;
    }

    static final String YES = "yes";

}
