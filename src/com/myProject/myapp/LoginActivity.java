
package com.myProject.myapp;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	//Declare database objects
	private MySQLDB database; 
	private Connection connection; 
	private Statement statement;
	private ResultSet results;
	private String retrieveSql = "";
	
	
	private Button signIn=null, cancel=null;
	private EditText text_email=null, text_password=null;
	private String uEmail=null, uPassword=null;
	
	private TextView uLoginStatusMessageView = null;
	private ProgressBar statusBar = null;
	
	private Sha1Conversion convertSHA;
	private UserLoginTask mAuthTask = null;
	private SessionManager session;
	private View focusView = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		//initialize the widgets here
		initializeUI();
		
		//attempt to signIn user when this button is pressed
		signIn.setOnClickListener(new View.OnClickListener() {					
			@Override
			public void onClick(View v) {								
				attemptLogin();		//check login credentials and start loggedInActivity on successful login								
				}
			});
				
		//finish activity when this button is clicked
		cancel.setOnClickListener(new View.OnClickListener() {					
			@Override
			public void onClick(View v) {
				finish();						
			}
		});	
	
	}//onCreate method ends here
	
	
	//method definition to initialize User Interface
	public void initializeUI(){
		statusBar = (ProgressBar) findViewById(R.id.progressBar1);
		statusBar.setVisibility(View.INVISIBLE);
		uLoginStatusMessageView = (TextView) findViewById(R.id.text_ProfileName);
		uLoginStatusMessageView.setVisibility(View.INVISIBLE);
		signIn = (Button) findViewById(R.id.button_Sign_In);
		cancel = (Button) findViewById(R.id.button_Cancel);
		text_email = (EditText) findViewById(R.id.email);
		text_password = (EditText) findViewById(R.id.password);	
		session = new SessionManager(getApplicationContext());
	}

		
	//AttemptLogin method
	public void attemptLogin() {		
	//step 1
		if (mAuthTask != null) {
			return;
		}
		
		// Reset errors.
		text_email.setError(null);
		text_password.setError(null);

		// Store values at the time of the login attempt.
		uEmail = text_email.getText().toString();
		uPassword = text_password.getText().toString();

		boolean cancel = false;
		
		// Check for a valid password.
		if (TextUtils.isEmpty(uPassword)) {
			text_password.setError(getString(R.string.error_field_required));
			focusView = text_password;
			cancel = true;
		} else if (uPassword.length() < 4) {
			text_password.setError(getString(R.string.error_invalid_password));
			focusView = text_password;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(uEmail)) {
			text_email.setError(getString(R.string.error_field_required));
			focusView = text_email;
			cancel = true;
		} else if (!uEmail.contains("@")) {
			text_email.setError(getString(R.string.error_invalid_email));
			focusView = text_email;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
				
			uLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			uLoginStatusMessageView.setVisibility(View.VISIBLE);
			statusBar.setVisibility(View.VISIBLE);
			
			//If everything is fine, start loginTask
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}
	//Attempt Login method ends here
	
	/**Class definition to asynchronously login user*/
	private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {			
			try {
				// Simulate network access delay.
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return false;
			}

			//get userName and password from database and authenticate the user
			try{
				database = new MySQLDB();
				connection = database.getConnection();
				statement = connection.createStatement();
				retrieveSql = "SELECT * FROM project.user_Info;";
				results = statement.executeQuery(retrieveSql);
				
				if(uEmail.matches(results.getString(2)) && (getPasswordHash(uPassword).matches(results.getString(3)))){
					return true;
				}
			}catch(SQLException sqle){
				sqle.printStackTrace();
			}		
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;			

			//if successfully logged In, open AddAccount Activity
			if (success) {
				//store the users credentials in shared preferences, so that can be used throughout the application
				saveCredentials();
				showToast("Your are Successfully loggedIn......");
				uLoginStatusMessageView.setVisibility(View.INVISIBLE);
				statusBar.setVisibility(View.INVISIBLE);
				Intent i = new Intent(LoginActivity.this, AddAccountActivity.class);
				startActivity(i);
				finish();
			} else {
				text_password.setError(getString(R.string.error_incorrect_password));
				text_password.requestFocus();
				showToast("Try Loggin in again ......");
				uLoginStatusMessageView.setVisibility(View.INVISIBLE);
				statusBar.setVisibility(View.INVISIBLE);
			}
		}
		@Override
		protected void onCancelled() {
			mAuthTask = null;			
		}
	}
	/**Asynchronous login task ends here...*/
	
	
	//method definition to encrypt the users password	
	public String getPasswordHash(String str){
		String key = "";
		convertSHA = new Sha1Conversion();
		try{
			key = convertSHA.makeSHA1Hash(str);
		}catch(Exception e){				
		}
			return key;
	}
	
	
	//store values to shared Preferences, so that this values can be used for future as well
	public void saveCredentials(){
		session.createLoginSession("Bigyan", "foo@example.com");
	}
	
	//method definition to showToast
	public void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}

/*...Login Activity Ends here...**/