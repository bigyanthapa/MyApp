package com.myProject.myapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LoggedInActivity extends Activity {

	//Database Declaration
	private SessionManager session;
	
	private String mEmail = null, mPassword = null;
	private Builder alert;
	private Spinner options1=null;
	private ListView friendList= null;
	
	private List<String> friendArrayList;
	private Button logoutBtn;
	private TextView profileView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logged_in);
		
		session = new SessionManager(getApplicationContext());
		
		// Populate the spinner
		addItemsOnSpinner();		
		
		// Receive data from previous activity
		String facebookName = getIntent().getStringExtra("facebookName");
		String facebookId = getIntent().getStringExtra("facebookId");
		String facebookEmail = getIntent().getStringExtra("facebookEmail");
		String [] friends = getIntent().getStringArrayExtra("friends");
		mEmail = getIntent().getStringExtra("userEmail");
		mPassword = getIntent().getStringExtra("userPassword");
		
		// Initialize User Interface
		profileView = (TextView) findViewById(R.id.profileView);
		profileView.setText(facebookName+" , "+facebookId);
		logoutBtn = (Button) findViewById(R.id.button1);
		friendList = (ListView) findViewById(R.id.friendView);		
		
		// Initialize friendlist
		friendArrayList = new ArrayList<String>(Arrays.asList(friends));		
	
		// Add items to the ListView
		populateListView();
		
		// Add actionListener to Spinner Item Selected
		addListenerOnSpinnerItemSelection();
		
		//when logout button is clicked, clear the user credentials and show the login window
		logoutBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				session.logoutUser();
				Intent i = new Intent(LoggedInActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}
		});
		
	}
	// onCreate Method ends here
	
	// Function Definition to add values to the ListView
	private void populateListView() {
		ArrayAdapter<String> adapter1 = new MyArrayAdapter();
		ListView friends = (ListView) findViewById(R.id.friendView);
		friends.setAdapter(adapter1);
	}

	// Function Definitino to Add items into spinner dynamically
	@SuppressWarnings("unchecked")
	public void addItemsOnSpinner() {		 
		 options1 = (Spinner) findViewById(R.id.profile_dropdown);
		 List list = new ArrayList();
		 list.add("All Friends");
		 list.add("Online");
		 list.add("Map View");		 
		 ArrayAdapter dataAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, list);
		 dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 options1.setAdapter(dataAdapter);
		 options1.setSelection(list.indexOf("All"));
	}
	
	// Function Definition to Add listener to spinner item selected
	public void addListenerOnSpinnerItemSelection() {
		options1 = (Spinner) findViewById(R.id.profile_dropdown);
		options1.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}
	
	// Function definition to showToast		
	public void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logged_in, menu);
		return true;
	}
	
	// Custom ArrayAdapter class for ading items in the ListView
	private class MyArrayAdapter extends ArrayAdapter<String>{
		public MyArrayAdapter(){
			super(LoggedInActivity.this,R.layout.friend_list_view, friendArrayList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//to make sure we have a view to work with
			View friendView = convertView;
			if(friendView == null){
				friendView = getLayoutInflater().inflate(R.layout.friend_list_view, parent, false);
			}
			
			// Display friend_list_view.xml from layout
			ImageView imageView = (ImageView)friendView.findViewById(R.id.friend_icon);
			imageView.setImageResource(R.drawable.friend_icon);
			
			// Now display name and id
			String[] fields = friendArrayList.get(position).split(",");
			
			TextView name_Text = (TextView) friendView.findViewById(R.id.text_ProfileName);
			name_Text.setText(fields[0]);
			
			TextView id_Text = (TextView) friendView.findViewById(R.id.text_Id);
			id_Text.setText(fields[1]);
			return friendView;
		}
		
	}

	// Class Definition to add Listener on Spinner Item Selected
	private class MyOnItemSelectedListener implements OnItemSelectedListener {
		
	@Override
	public void onItemSelected(AdapterView parent, View view, int pos, long id) {		 
		if(parent.getItemAtPosition(pos).toString().matches("Map View")){
			// Display MapView Activity when this option is selected
			Intent i = new Intent(LoggedInActivity.this, ViewOnMap.class);
			startActivity(i);
		}		
	}
	
	@Override
	public void onNothingSelected(AdapterView parent) {
		
	}
		
	}
}
/** ... Logged In Activity Ends Here ...*/