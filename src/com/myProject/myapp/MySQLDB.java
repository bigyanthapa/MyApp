package com.myProject.myapp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDB {
	private Connection con;

	private String _dblocation;
	private String _dbusername;
	private String _dbpassword;
	private String _dbschema;
	private String _dbport;
	
	public MySQLDB(){
		try{
			_dblocation = "msc-project.cporl4uo4azt.eu-west-1.rds.amazonaws.com";
			_dbusername = "notroot";
			_dbpassword = "Amazon2013#";
			_dbschema = "project";
			_dbport = "" + "3306";
		
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + _dblocation + ":" + _dbport + "/" + _dbschema;
			con = DriverManager.getConnection(url,_dbusername,_dbpassword);
		}catch (SQLException sqlex) {
			System.out.println("Problem with connection to schema " + _dbschema + ". " + sqlex.getMessage());
		}catch (ClassNotFoundException cnfe) {
			System.out.println("Problem with class: " + cnfe.getMessage());
		}
	}
	
	public MySQLDB(String hostname, String username, String password, String schema, int port){
		try{
			_dblocation = hostname;
			_dbusername = username;
			_dbpassword = password;
			_dbschema = schema;
			_dbport = "" + port;
		
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + _dblocation + ":" + _dbport + "/" + _dbschema;
			con = DriverManager.getConnection(url,_dbusername,_dbpassword);
		}catch (SQLException sqlex) {
			System.out.println("Problem with connection to schema " + _dbschema + ". " + sqlex.getMessage());
		}catch (ClassNotFoundException cnfe) {
			System.out.println("Problem with class: " + cnfe.getMessage());
		}
	}
	
	public boolean close(){
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Connection getConnection() {
		return con;
	}
	
	public static String sqlify(String statement){
		if(statement != null){
			statement.replaceAll("'", "");
			return statement;
		}else{
			return "null";
		}
	}
	
	
	/**
	 * 	MYSQL ESCAPED CHARACTERS
	 *	------------------------------------------------
	 *	\0  An ASCII NUL (0x00) character.	//IRRELEVANT - NOT IN STRING IF NOT NULL
	 *	\' 	A single quote (�'�) character.
	 *	\" 	A double quote (�"�) character.
	 *	\b 	A backspace character.			//SAME AS JAVA
	 *	\n 	A newline (linefeed) character. //SAME AS JAVA
	 *	\r 	A carriage return character.	//SAME AS JAVA
	 *	\t 	A tab character.				//SAME AS JAVA
	 *	\Z 	ASCII 26 (Control-Z). See note following the table. //IRRELEVANT - NOT IN STRING
	 *	\\ 	A backslash (�\�) character.
	 *	\% 	A �%� character. See note following the table.
	 *	\_ 	A �_� character. See note following the table.
	 */
	public String statementFromString(String sql){
		sql.replaceAll("'", "\\'");		//			'
		sql.replaceAll("\"", "\\\"");	//			"
//		sql.replaceAll("\\", "\\\\");	//			\
		sql.replaceAll("%", "\\%");		//			%
		sql.replaceAll("_", "\\_");		//			_
		
		if(!sql.endsWith(";")){
			sql = sql + ";";
		}
		return sql;
	}
}
