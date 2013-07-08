package org.cloudcoder.analysis.features.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

public class UpdateFeaturesTable {
	
	private static LinkedList<String> features = new LinkedList<String>();

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		setUpFeatures();
		putInDB();
	}
	
	public static void setUpFeatures(){
		// Features added on 04/10/2013
		features.add("return_comparison");
		features.add("return_and");
		features.add("return_or");
		features.add("return_complex_logic");
		features.add("return_function");
		features.add("if_and_simple");
		features.add("if_or_simple");
		features.add("if_and_complex");
		features.add("if_or_complex");
		
		// Features added on 04/09/2013
		features.add("len-k_stop_for_loop");
    	features.add("len-var_stop_for_loop");
    	features.add("return_true_in_loop");
    	features.add("return_false_in_loop");
    	features.add("return_arithmetic");
    	features.add("return_variable");
    	features.add("return_literal");
    	features.add("if_no_else");
    	features.add("if_else");
    	features.add("if_else-if");
    	features.add("if_else-if_else");
    	features.add("if_else-if_else-if");
    	features.add("if_else-if_else-if_else");
    	features.add("if_k_else-if");
    	features.add("if_k_else-if_else");
    	features.add("if_nested_in_then");
    	
		
		// Features added on 01/24/2013
		features.add("array");
    	features.add("break_stmt");
    	features.add("++_for_loop");
    	features.add("--_for_loop");
    	
    	//TODO MAKE += NOT 1
    	features.add("+=_for_loop");
    	features.add("-=_for_loop");
    	
    	features.add("nested_for_loop");
    	features.add("if_stmt");
    	features.add("if_stmt_loop");
    	features.add("len_stop_for_loop");
    	features.add("len-1_stop_for_loop");
    	features.add("method_declaration");
    	features.add("no_block_if_stmt");
    	features.add("nonzero_start_for_loop");
    	features.add("return_stmt");
    	features.add("return_stmt_loop");
    	features.add("parameter");
    	features.add("switch_stmt");
    	features.add("throw_stmt");
    	features.add("try_stmt");
    	features.add("var_declaration");
    	features.add("var_start_for_loop");
    	features.add("while_loop");
    	features.add("catch_stmt");
    	features.add("++_stmt");
    	features.add("--_stmt");
    	features.add(">_stop_for_loop");
    	features.add(">=_stop_for_loop");
    	features.add("<_stop_for_loop");
    	features.add("<=_stop_for_loop");
	}
	
	public static void putInDB() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		Connection conn=new MyConnection().getConnection();
        PreparedStatement pstmt = null;
        
        try {

        	System.out.println("Got database connection!");
        	
        	for(String f : features){
	        	String query = 
	        			"insert into a_features"+
	        		    "(name) "+
	        			"values(?)";
	
	        	pstmt = conn.prepareStatement(query); // create a statement
	        	pstmt.setString(1, f);
	        	
	        	try {
	        		pstmt.executeUpdate(); // execute insert statement
	        	}catch(Exception e){
	        		System.out.println("Query failed for value '"+f+"'.");
	        	}
        	}
        	
        } finally {
        	
          if (pstmt!=null) pstmt.close();
          if (conn!=null) conn.close();
          System.out.println("The queries have been completed.");
          
        }
	}
	
}
