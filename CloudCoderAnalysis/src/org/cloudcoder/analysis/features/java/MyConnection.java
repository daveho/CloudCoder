package org.cloudcoder.analysis.features.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
	
	public Connection getConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
        String database="__cc_data";
        String username="root";
        String password="";
        String host="localhost";
        String port="3306";
        
        //Class.forName("com.mysql.jdbc.Driver").newInstance(); 
        String dbServer="jdbc:mysql://" + host+ ":" + port + "/" + database;
        return DriverManager.getConnection(dbServer, username, password);
	}
	
}
