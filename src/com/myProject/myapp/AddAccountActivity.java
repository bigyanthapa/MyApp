package com.myProject.myapp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.Properties;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.SimpleFacebook.OnFriendsRequestListener;
import com.sromku.simple.fb.SimpleFacebook.OnLoginListener;
import com.sromku.simple.fb.SimpleFacebook.OnLogoutListener;
import com.sromku.simple.fb.SimpleFacebook.OnProfileRequestListener;
import com.sromku.simple.fb.entities.Profile;

public class AddAccountActivity extends Activity {
	
	//Declare database objects
	private MySQLDB database; 
	private Connection connection; 
	private Statement statement;
	private ResultSet results;
	private String insertUserInfo,insertLocation, retrieveSql = "";
		
	//Declare other required class objects
	private GPSTracker gps;
	private ProgressDialog mProgress;	
	protected static final String TAG = AddAccountActivity.class.getName();
	
	//Fields required for Facebook
	private SimpleFacebook mSimpleFacebook;
	private static final String APP_ID = "1375926665995831";
	private static final String APP_NAMESPACE = "projectapplication";
	
	// Initial permissions for facebook integration
	Permissions[] permissions = new Permissions[]
	{
		Permissions.BASIC_INFO,
		Permissions.EMAIL,
		Permissions.USER_PHOTOS,
		Permissions.USER_BIRTHDAY,
		Permissions.USER_LOCATION,
		Permissions.PUBLISH_ACTION,
		Permissions.PUBLISH_STREAM,
		Permissions.USER_LOCATION,
		Permissions.FRIENDS_LOCATION
	};

	// Define configuration settings for facebook login
	SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
		.setAppId(APP_ID)
		.setNamespace(APP_NAMESPACE)
		.setPermissions(permissions)
		.setDefaultAudience(SessionDefaultAudience.FRIENDS)
		.build();	
	
	// Declare the properties required while receiving information from facebook
	Properties properties = new Properties.Builder()
			.add(Properties.ID)
			.add(Properties.FIRST_NAME)
			.add(Properties.LOCATION)
			.build();
	
	// Declare variables
	private String email, name, gender, passwordHash, user_id;
	private String facebookName, facebookId, facebookEmail;
	private double mLatitude, mLongitude;
	private boolean isFacebook;
	
	// Declare UI Widgets
	private ImageButton facebookButton;
	private TextView messageView1;
	private Button logoutBtn, nextBtn, skipBtn;
	
	// These fields are required for facebook friend list
	private List<String> values ;	
	private String [] friends = {};
	
	// Definition for Login Listener
	private OnLoginListener mOnLoginListener = new OnLoginListener()
	{
		@Override
		public void onFail(String reason)
		{
			messageView1.setText(reason);
			Log.w(TAG, "Failed to login");
		}

		@Override
		public void onException(Throwable throwable)
		{
			messageView1.setText("Exception: " + throwable.getMessage());
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking()
		{
			// show progress bar or something to the user while login is happening
			messageView1.setText("Thinking...");
		}

		@Override
		public void onLogin()
		{	
			showToast("You are successfully loggedIn to Facebook.");
			// set loggedIn State
			mSimpleFacebook.getFriends(mOnFriendsRequestListener);			
			mSimpleFacebook.getProfile(onProfileRequestListener);
			loggedInState();
		}

		@Override
		public void onNotAcceptingPermissions()
		{
			messageView1.setText("You didn't accept read permissions");
			showToast("You didn't accept read permissions");
		}
	};
	
	// Logout listener
	private OnLogoutListener mOnLogoutListener = new OnLogoutListener()
	{
		@Override
		public void onFail(String reason)
		{
			messageView1.setText(reason);
			Log.w(TAG, "Failed to login");
		}

		@Override
		public void onException(Throwable throwable)
		{
			messageView1.setText("Exception: " + throwable.getMessage());
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking()
		{
			// show progress bar or something to the user while login is happening
			messageView1.setText("Thinking...");
		}

		@Override
		public void onLogout()
		{
			// change the state of the button or do whatever you want			
			showToast("You are logged out");
			// set logged out state
			loggedOutState();
			//clear data
			clearFriendList();
		}

	};
	// get friends listener
	final private OnFriendsRequestListener mOnFriendsRequestListener = new SimpleFacebook.OnFriendsRequestListener()
	{
		@Override
		public void onFail(String reason)
		{
			// insure that you are logged in before getting the friends
			Log.w(TAG, reason);
		}

		@Override
		public void onException(Throwable throwable)
		{
			Log.e(TAG, "Bad thing happened", throwable);
		}
		
		@Override
		public void onThinking()
		{
			// show progress bar or something to the user while fetching profile
			Log.i(TAG, "Thinking...");		    
		}

		@Override
		public void onComplete(List<Profile> friends)
		{
			// populate list
			values = new ArrayList<String>();
			for (Profile profile: friends)
			{
				values.add(profile.getName()+" , "+profile.getId()+" , "+profile.getLocation());
			}			
		}

	};
	
	// Listener for profile request
	final private OnProfileRequestListener onProfileRequestListener = new SimpleFacebook.OnProfileRequestListener()
	{
		@Override
		public void onFail(String reason)
		{
			// insure that you are logged in before getting the profile
			Log.w(TAG, reason);
		}
		@Override
		public void onException(Throwable throwable)
		{			
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking()
		{				
			// show progress bar or something to the user while fetching profile
			Log.i(TAG, "Thinking...");
		}

		@Override
		public void onComplete(Profile profile)
		{			
			Log.i(TAG, "My profile id = " + profile.getId());
			facebookName = profile.getName();
			facebookEmail = profile.getEmail();
			facebookId = profile.getId();
		}
	};
		
	// OnCreate Method definition
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_account);
		
		SimpleFacebook.setConfiguration(configuration);		
		
		initializeUI();
		
		// Define onClickListener when facebookButton is clicked
		facebookButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Try Facebook Login here				
				mSimpleFacebook.login(mOnLoginListener);				
			}
		});
		
		// Define onClickListener when logout button is clicked
		logoutBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mSimpleFacebook.logout(mOnLogoutListener);				
			}
		});
		
		// when the next button is clicked, insert user information in database and start loggedInActivity
		nextBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				friends = getFriends();							
				name = getIntent().getStringExtra("name");
				email = getIntent().getStringExtra("email");
				gender = getIntent().getStringExtra("gender");
				passwordHash = getIntent().getStringExtra("passwordHash");
			    user_id = getAppId();				
				
				// Get User Location and Proceed
			    getUserLocationAndProceed();
			    			
			}
		});		
		
	}//onCreate ends here
	
	// Function definition to 
	public void initializeUI(){
		nextBtn = (Button) findViewById(R.id.nextBtn);
		skipBtn = (Button) findViewById(R.id.skipBtn);
		logoutBtn = (Button) findViewById(R.id.logoutBtn);
		facebookButton = (ImageButton) findViewById(R.id.facebookBtn);
		messageView1 = (TextView) findViewById(R.id.statusFacebook);		
	}
	
	
	public String[] getFriends(){
		String [] arr = values.toArray(new String[values.size()]);
		return arr;
	}
	
	/** generate an appId for the user */	
	public String getAppId(){
		// Usually this can be a field rather than a method variable
	    Random rand = new Random();
	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((100000 - 1) + 1) + 1;
		
		return "user"+randomNum;
	}	
	
	/**..This is function definition */
	public void insertUserData(String name, String email, String passwordHash, String gender, String user_id, double latitude, double longitude) {
		showToast("Connecting to database, please wait......");
		/** Get current time stamp.*/
		String time = getTime();
		try{			
		database = new MySQLDB();
		connection = database.getConnection();
		statement = connection.createStatement();
		insertUserInfo = "insert into user_Info values('" + name + "','" + email + "','" + passwordHash + "','" + gender + "','" + user_id + "')";
		insertLocation = "insert into location_Table values('" + latitude + "','" + longitude + "','" + user_id + "','" + time + "')";
		// statement.execute(insertSql);
		// 	statement.execute(insertLocation);
	
		// These are test Statements here
		/***	retrieveSql = "SELECT * FROM project.location_Table;";
		results = statement.executeQuery(retrieveSql);
			while(results.next()){				
				String u_name = results.getString("user_name");
				String p_word = results.getString("user_passwordHash");
				showToast(u_name+" "+p_word+" ");	
				showToast(time);
			}***/			
	
		}
		catch(SQLException sqle){
		sqle.printStackTrace();
		}
	}
	
	
	// Function definition to get current timeStamp	
	@SuppressLint("SimpleDateFormat")
	public String getTime(){		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//get current date time with Calendar()
		   Calendar cal = Calendar.getInstance();
		   return dateFormat.format(cal.getTime()).toString();			   
	}
	
	// Initialize facebook onActivity Resume
	@Override
	protected void onResume()
	{
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);	
		setUIState();
	}

	/***
	 *  GET USER LOCATION 
	 *  get location data from GPSTracker.java ** **/
	
	protected void getUserLocationAndProceed() 
	{  
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		  builder.setMessage("Your application wants to access your GPS data !!")  
		  .setCancelable(false)  
		  .setTitle("** Gps Access **")  
		  .setPositiveButton("Ok",  
		   new DialogInterface.OnClickListener() {  
		   public void onClick(DialogInterface dialog, int id) {  
		   // finish the current activity  
		   // AlertBoxAdvance.this.finish();		   
			   gps = new GPSTracker(AddAccountActivity.this);		
				// check if GPS enabled    
		        if(gps.canGetLocation()){		             
		            mLatitude = gps.getLatitude();
		            mLongitude = gps.getLongitude(); 		            
		            
					insertUserData(name, email, passwordHash, gender, user_id, mLatitude, mLongitude);				
					Intent i = new Intent (AddAccountActivity.this, LoggedInActivity.class);				
					i.putExtra("facebookName", facebookName);
					i.putExtra("facebookId", facebookId);
					i.putExtra("facebookEmail", facebookEmail);
					i.putExtra("friends", friends);
	            
					startActivity(i);
					finish();	
		        }else{
		            // If location cannot be retrieved, show alert to user
		            gps.showSettingsAlert();
		        }
	        
		        dialog.cancel();  
		   }		  
		   })  
		   .setNegativeButton("Cancel",  
		   new DialogInterface.OnClickListener() {  
		   public void onClick(DialogInterface dialog, int id) {  
		    // cancel the dialog box
			  showToast("You denied to provide access to location. Your location is not initialized.");
			  mLatitude = 0.0;
			  mLongitude = 0.0;			  
			  		
			  insertUserData(name, email, passwordHash, gender, user_id, mLatitude, mLongitude);				
			  Intent i = new Intent (AddAccountActivity.this, LoggedInActivity.class);				
			  i.putExtra("facebookName", facebookName);
			  i.putExtra("facebookId", facebookId);
			  i.putExtra("facebookEmail", facebookEmail);
			  i.putExtra("friends", friends);
          
			  startActivity(i);
			  finish();	
			  dialog.cancel();  
		    }  
		   });  
		  AlertDialog alert = builder.create();  
		  alert.show(); 		  
    }
	
	
	
	// onActivityResult to initialize Facebook
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	// Function Definition to set Initial UI
	public void setUIState(){
		if (mSimpleFacebook.isLogin())
		{
			loggedInState();
		}
		else
		{		
			loggedOutState();
		}		
	}
	
	// Logged In State
	public void loggedInState(){
		skipBtn.setEnabled(false);
		nextBtn.setEnabled(true);
		facebookButton.setEnabled(false);
		messageView1.setText("Logged In");
		logoutBtn.setEnabled(true);
		logoutBtn.setVisibility(View.VISIBLE);
		mSimpleFacebook.getFriends(mOnFriendsRequestListener);				
		mSimpleFacebook.getProfile(onProfileRequestListener);
	}
	
	// Logged Out State
	public void loggedOutState(){
		messageView1.setText("Logged Out");
		logoutBtn.setVisibility(View.INVISIBLE);
		facebookButton.setEnabled(true);
		logoutBtn.setEnabled(false);
		skipBtn.setEnabled(true);
		nextBtn.setEnabled(false);
	}
	
	//clear friends list
	public void clearFriendList(){
		// when logout is clicked, clear all the friend list
		values.clear();
		friends=null;
		
	}
	// show and hide progress dialog
	private void showDialog()
	{
		mProgress = ProgressDialog.show(this, "Thinking",
			"Waiting for Facebook", true);
	}

	private void hideDialog()
	{
		mProgress.hide();
	}

	//method definition to showToast
	public void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_account, menu);
		return true;
	}

}

/** ... AddAccountActivity Ends Here ...*/