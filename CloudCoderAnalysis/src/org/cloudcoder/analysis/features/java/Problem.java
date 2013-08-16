package org.cloudcoder.analysis.features.java;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class Problem {

	private String name;
	private int id;
	private int courseId;
	private String description;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCourseId() {
		return courseId;
	}
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LinkedList<Submission> getSubmissionsFromDB() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		LinkedList<Submission> submissions = new LinkedList<Submission>();
		Connection conn = null;
        Statement pstmt = null;

        try {

        	conn = new MyConnection().getConnection();
        	//System.out.println("Got database connection!");
        	
        	String query = 
        			"select sub.event_id AS id,sub.num_tests_attempted AS testsAttempted," +
        			"sub.num_tests_passed AS testsPassed,ev.problem_id AS problemId," +
        			"ch.text AS source,ev.user_id AS userId " +
        			"from cc_submission_receipts sub,cc_events ev,cc_changes ch "+
        		    "where sub.last_edit_event_id = ev.id AND ev.problem_id = "+this.getId()+" AND "+
        		    "ch.event_id = ev.id AND ch.type = 4";
        	
        	pstmt = conn.createStatement(); // create a statement
        	ResultSet rs = pstmt.executeQuery(query); // execute insert statement

        	while(rs.next()){
        		Submission s = new Submission();
        		s.setId(rs.getInt("id"));
        		s.setProblemId(this.getId());
        		s.setTestsAttempted(0);//rs.getInt("testsAttempted"));
        		s.setTestsPassed(0);//rs.getInt("testsPassed"));
        		s.setUserId(rs.getInt("userId"));
        		s.setSource(wrapMethodWithClass(rs.getString("source")));
        		submissions.add(s);
        	}
        	
        } finally {
        	
          if (pstmt!=null) pstmt.close();
          if (conn!=null) conn.close();
          System.out.println("Extracted all submissions ("+submissions.size()+") for problem "+this.getId()+".");
          
        }
		return submissions;
	}
	private String wrapMethodWithClass(String string) {
		return "public class Test { "+string+" }";
	}

}
