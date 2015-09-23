
package com.myProject.myapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	//Declare all the UI widgets
	private EditText name_field, email_field, password_field, confirm_password_field;
	private RadioGroup genderRG;
	private Button next_button, back_button;
	private TextView registerStatusMessage;
	private ProgressBar progress;
	private View focusView = null;
	
	//Declare the variables
	private String name, email, passwordHash, gender;	
	private double latitude, longitude;
	
	//Create objects of necessary classes
	private GPSTracker gps;
	private Sha1Conversion convertSHA;	
	
	
	//starting onCreate method
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		// Initialize the UI 
		initializeUI();
		
		//Hide the text message and progress bar in the beginning
		registerStatusMessage.setVisibility(View.INVISIBLE);
		progress.setVisibility(View.INVISIBLE);
		
		//Now set action listener to the buttons
		next_button.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				//Set UI to onProgress, i.e. inactive
				registrationOnProgress();
				
				//Then attempt to register the new user
				attemptRegistration();
			}
		});
		
		//Implement actionListener for back_button
		back_button.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
			}
		});	
		
	}//onCreate method ends here
	
	
	//Method Definition to initialize the UI
	public void initializeUI(){
		next_button = (Button) findViewById(R.id.button_next);
		back_button = (Button) findViewById(R.id.button_back);
		registerStatusMessage = (TextView) findViewById(R.id.registerMessage);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		name_field = (EditText) findViewById(R.id.name_input);
		email_field = (EditText) findViewById(R.id.email_input);
		password_field = (EditText) findViewById(R.id.password_input);
		confirm_password_field = (EditText) findViewById(R.id.confirm_password_input);		
		genderRG = (RadioGroup) findViewById(R.id.genderRadioGroup);
	}
	
	//Method Defintion to set registrationOnProgress UI
	public void registrationOnProgress(){
		next_button.setEnabled(false);
		back_button.setEnabled(false);	
		name_field.setEnabled(false);
		email_field.setEnabled(false);
		password_field.setEnabled(false);
		confirm_password_field.setEnabled(false);
		genderRG.setEnabled(false);
	}
	
	 // Method Definition to register new user
	public void attemptRegistration(){
		
		// Reset errors for all fields
		name_field.setError(null);
		email_field.setError(null);
		password_field.setError(null);
		confirm_password_field.setError(null);

		// extract the input values
		name = name_field.getText().toString();
		email = email_field.getText().toString();
		passwordHash = "";
		gender = getGender();
		
		boolean cancel = false;		
		
		// Check for a valid email address.
		if (TextUtils.isEmpty(name_field.getText().toString().trim())) {
			email_field.setError(getString(R.string.error_field_required));
			focusView = email_field;
			cancel = true;
		} else if (!email.contains("@")) {
			email_field.setError(getString(R.string.error_invalid_email));
			focusView = email_field;			
			cancel = true;
		}

		// Check for a valid password.
		if ((TextUtils.isEmpty(password_field.getText().toString().trim())) || (TextUtils.isEmpty(confirm_password_field.getText().toString().trim()))) {
			password_field.setError(getString(R.string.error_field_required));
			focusView = password_field;
			cancel = true;
		} 
		else if(!password_field.getText().toString().matches(confirm_password_field.getText().toString()))
		{
			password_field.setError(getString(R.string.error_mismatch_password));					
			focusView = password_field;
			cancel = true;
		}else{
			passwordHash = getPasswordHash(password_field.getText().toString());
		}
		
		//If error set focus to the same activity
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			setUI();
			showToast("Problem !! Please try with correct values....");
			name_field.requestFocus();			
		} else {		
			registerStatusMessage.setText("User Registration in progress, please wait ...");
			registerStatusMessage.setVisibility(View.VISIBLE);
			progress.setVisibility(View.VISIBLE);	
			
			/** NOW PROGRESS TO THE NEXT ACTIVITY */
			showToast("Your are Successfully Registered......");	        
			registerStatusMessage.setVisibility(View.INVISIBLE);
			progress.setVisibility(View.INVISIBLE);
			Intent i = new Intent (RegisterActivity.this, AddAccountActivity.class);
			i.putExtra("name", name);
			i.putExtra("email", email);
			i.putExtra("gender", gender);
			i.putExtra("passwordHash", passwordHash);			
        
			startActivity(i);
			finish();		 
			
			//Now retrieve the user current location and proceed registration
			//retrieveLocationNProceed();			
		}
		
	}//attemptRegistration ends here
	
	
	/*----------Method to create an AlertBox ------------- */
	/* get location data from GPSTracker.java */	
	protected void retrieveLocationNProceed() 
	{  
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		  builder.setMessage("Your application wants to access your GPS data !!")  
		  .setCancelable(false)  
		  .setTitle("*** Gps Access ***")  
		  .setPositiveButton("Ok",  
		   new DialogInterface.OnClickListener() {  
		   public void onClick(DialogInterface dialog, int id) {  
			   //initialize the object of GPSTracker class
			   gps = new GPSTracker(RegisterActivity.this);		
				// check if GPS enabled    
		        if(gps.canGetLocation()){		             
		            latitude = gps.getLatitude();
		            longitude = gps.getLongitude();       
		        }else{
		            // If location cannot be retrieved, show alert to user
		            gps.showSettingsAlert();
		        }

		        // let the user know of Successful Registration
		        showToast("Your are Successfully Registered......");
		        
				registerStatusMessage.setVisibility(View.INVISIBLE);
				progress.setVisibility(View.INVISIBLE);
				Intent i = new Intent (RegisterActivity.this, AddAccountActivity.class);
				i.putExtra("name", name);
				i.putExtra("email", email);
				i.putExtra("gender", gender);
				i.putExtra("passwordHash", passwordHash);
				i.putExtra("latitude", latitude);
				i.putExtra("longitude", longitude);
            
				startActivity(i);
				finish();		    
		        
		        dialog.cancel();  
		   }		  
		   })  
		   .setNegativeButton("Cancel",  
		   new DialogInterface.OnClickListener() {  
		   public void onClick(DialogInterface dialog, int id) {  
		    // cancel the dialog box
			  showToast("You denied to provide access to location. Your location is not initialized.");
			  latitude = 0.0;
			  longitude = 0.0;
			  
			  showToast("Your are Successfully Registered......");
				registerStatusMessage.setVisibility(View.INVISIBLE);
				progress.setVisibility(View.INVISIBLE);
				Intent i = new Intent (RegisterActivity.this, AddAccountActivity.class);
				i.putExtra("name", name);
				i.putExtra("email", email);
				i.putExtra("gender", gender);
				i.putExtra("passwordHash", passwordHash);
				i.putExtra("latitude", latitude);
				i.putExtra("longitude", longitude);          
				
				startActivity(i);
				finish();
			  dialog.cancel();  
		    }  
		   });  
		  AlertDialog alert = builder.create();  
		  alert.show(); 		  
    }
	//Method definition to show AlertBox ends here
	
	//Method Definition to retrieve the user's Gender
	String gender1="";
	int checkId;
	int checkId1;
	int check = 3; //if not checked neutral 
	public String getGender(){			
		
		int radioButtonID = genderRG.getCheckedRadioButtonId();
		View radioButton = genderRG.findViewById(radioButtonID);
		int indx = genderRG.indexOfChild(radioButton);
	
		//return the gender
		if(indx == 0){
			return "Male";
		}else if(indx == 1){
			return "Female";
		}else{
			return "Neutral";
		}			
	}
	//get gender method ends here
	
	
	//encrypt the password	
	public String getPasswordHash(String str){
		String key = "";
		convertSHA = new Sha1Conversion();
		try{
			key = convertSHA.makeSHA1Hash(str);
		}catch(Exception e){				
		}
			return key;
	}
	
	//set User Interface
	public void setUI(){
		next_button.setEnabled(false);
		back_button.setEnabled(true);
		registerStatusMessage.setVisibility(View.INVISIBLE);
		progress.setVisibility(View.INVISIBLE);
		name_field.setText("");
		email_field.setText(""); 
		password_field.setText("");
		confirm_password_field.setText("");	
		name_field.setEnabled(true);
		email_field.setEnabled(true);
		password_field.setEnabled(true);
		confirm_password_field.setEnabled(true);
		genderRG.setEnabled(true);
		
	}
	
	//method definition to showToast
	public void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
/** ... RegisterActivity Ends Here ...*/