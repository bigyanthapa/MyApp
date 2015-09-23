

package com.myProject.myapp;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.myProject.myapp.R.color;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	//Database Declaration
		MySQLDB database; 
		Connection connection; 
		Statement statement;
		ResultSet results;
		String insertSql, retrieveSql = "";
	
	private SessionManager session;
	//declare the widgets
	private Button logIn, signUp = null;
	private TextView welcomeMsg = null;
	
	//program entry point
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		//set the title of the activity
		setTitle("TiNy wOrLd");
		
		//initialize the userInterface
		initializeUI();
		
		//setOnClickListener for logIn Button
		/* when login button is pressed, it is presumed that the user is already a member, so is directed towards addAccount activity
		 * */
		logIn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				//deleteAllFromTable();
				
				// check if the user is already logged in
				checkPreferences();								
			}
		});
		
		//setOnclickListener for signUp button
		/*when signUp is pressed, it is presumed that the user is a new user and is directed towards registerActivity*/
		signUp.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent registerActivity = new Intent(MainActivity.this, RegisterActivity.class);
				startActivity(registerActivity);				
			}
		});			
	}
	
	//function definition to initialize the User Interface
	public void initializeUI(){
		logIn  = (Button) findViewById(R.id.logIn);
		signUp = (Button) findViewById(R.id.signUp);
		welcomeMsg = (TextView) findViewById(R.id.welcomeMsg);
		session = new SessionManager(getApplicationContext());
	}
	
	//finish the application when the system back button is pressed
	@Override
	public void onBackPressed(){
		finish();
	}
	
	//function to check if there are any saved sharedPreferences
	public void checkPreferences(){
		if(session.isLoggedIn()){			
			Intent addAccount = new Intent(MainActivity.this, AddAccountActivity.class);
			startActivity(addAccount);
		}else{
			Intent addAccount = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(addAccount);
		}
	}
	
	//function definition to delete all values from database
	public void deleteAllFromTable(){
		//get userName and password from database and authenticate the user
		try{
			database = new MySQLDB();
			connection = database.getConnection();
			statement = connection.createStatement();
			String query = "TRUNCATE TABLE project.location_Table;";
			statement.execute(query);					
			
		}catch(SQLException sqle){
			sqle.printStackTrace();
		}
		
	}
	
	//method definition to showToast
	public void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}		
	
}

/**...MainActivity Ends Here...*/
