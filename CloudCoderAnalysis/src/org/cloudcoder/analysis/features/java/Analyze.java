package org.cloudcoder.analysis.features.java;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;

public class Analyze {

	public static void main(String[] args) throws Exception {
		/**
		Connection conn = new MyConnection().getConnection();
		FeatureVisitor visitor=new FeatureVisitor();
		TreeMap<String,Feature> features = getFeaturesFromDB(conn);
		visitor.setFeatures(features);
		Submission s = new Submission();
		String myFile = new Scanner(new File("testing/features/java/A1.java")).useDelimiter("\\Z").next();
		s.setId(1);
		s.setProblemId(1);
		s.setSource(myFile);
		s.setTestsAttempted(5);
		s.setTestsPassed(5);
		s.setUserId(1);
        visitor.extractFeatures(s);
        visitor.print();
		**/
		Connection conn = new MyConnection().getConnection();
		LinkedList<Problem> problems = getProblemsFromDB(conn);
		conn = new MyConnection().getConnection();
		TreeMap<String,Feature> features = getFeaturesFromDB(conn);
		for(Problem p : problems){
			LinkedList<Submission> submissions = p.getSubmissionsFromDB();
			for(Submission s : submissions){
				FeatureVisitor visitor=new FeatureVisitor();
				visitor.setFeatures(features);
		        putDataInDatabase(s,visitor.extractFeatures(s));
			}
		}
	}
	
	public static void putDataInDatabase(Submission s, HashMap<Feature, Integer> hashMap) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    	Connection conn=new MyConnection().getConnection();
        PreparedStatement pstmt = null;
        try {

        	//System.out.println("Processing submission ID "+s.getId()+":");
        	
        	for(Feature f : hashMap.keySet()){
        		int n = hashMap.get(f);
        		for(int i=0; i<n; i++){
		        	String query = 
		        			"insert into a_features_in_submissions"+
		        		    "(feature_id,submission_id) "+
		        			"values(?, ?)";
		
		        	pstmt = conn.prepareStatement(query); // create a statement
		        	
		        	pstmt.setInt(1, f.getId());
		        	pstmt.setInt(2, s.getId());
		        	
		        	pstmt.executeUpdate(); // execute insert statement
        		}
        	}
        	
        } finally {
        	
          if (pstmt!=null) pstmt.close();
          if (conn!=null) conn.close();
          //System.out.println("The queries have been completed.");
          
        }
	}
	
	public static LinkedList<Problem> getProblemsFromDB(Connection c) throws SQLException{
		LinkedList<Problem> problems = new LinkedList<Problem>();
		Connection conn=c;
        Statement pstmt = null;
        
		try {
        	
        	System.out.println("Got database connection!");
        	
        	String query = 
        			"select p.problem_id AS id,p.course_id AS courseId," +
        			"p.brief_description AS description,p.testname AS name " +
        			"from cc_problems AS p";

        	pstmt = conn.createStatement(); // create a statement
        	ResultSet rs = pstmt.executeQuery(query); // execute insert statement
        	
        	while(rs.next()){
        		Problem p = new Problem();
        		p.setId(rs.getInt("id"));
        		p.setCourseId(rs.getInt("courseId"));
        		p.setName(rs.getString("name"));
        		p.setDescription(rs.getString("description"));
        		problems.add(p);
        	}
        	
        } finally {
        	
          if (pstmt!=null) pstmt.close();
          if (conn!=null) conn.close();
          System.out.println("Extracted all problems ("+problems.size()+").");
          
        }
		return problems;
	}
	
	public static TreeMap<String,Feature> getFeaturesFromDB(Connection c) throws SQLException{
		TreeMap<String,Feature> map = new TreeMap<String,Feature>();
		Connection conn=c;
        Statement pstmt = null;
        
		try {
        	
        	System.out.println("Got database connection!");
        	
        	String query = 
        			"select * " +
        			"from a_features";

        	pstmt = conn.createStatement(); // create a statement
        	ResultSet rs = pstmt.executeQuery(query); // execute insert statement
        	
        	while(rs.next()){
        		Feature f = new Feature();
        		f.setId(rs.getInt("id"));
        		f.setName(rs.getString("name"));
        		map.put(f.getName(), f);
        	}
        	
        } finally {
        	
          if (pstmt!=null) pstmt.close();
          if (conn!=null) conn.close();
          System.out.println("Extracted all features ("+map.size()+").");
          
        }
		return map;
	}

}
