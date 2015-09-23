package com.myProject.myapp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewOnMap extends Activity {
	
	//Database Object Declaration
	private MySQLDB database; 
	private Connection connection; 
	private Statement statement;
	private ResultSet results;
	private String insertSampleQuery;
	
	private double latitude =0.0;
	private double longitude = 0.0;
	private double[] lat;
	private double[] lon;
	
	// Google Map
    private GoogleMap googleMap;
    private LatLng myLocation, friendLocation;
    
    //declare widgets
    private Button search_Button;
    private EditText search_Range;
    
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);
        
        /** This is for testing purpose */
        //populate sample location table
        //populateSampleLocationTable();      
      	
        // Initialize UI
        search_Range = (EditText) findViewById(R.id.search_Radius);
        search_Range.setInputType(InputType.TYPE_CLASS_NUMBER); //allow input type only integer        
        search_Button = (Button) findViewById(R.id.search_Button);
        
        // Set ActionListener for Search Button
        search_Button.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				search_Range.clearFocus();
				int r = Integer.parseInt(search_Range.getText().toString());
				search_Range.setText("");				
				drawCircle(r);								
			}
		});
        
       
 
       // Get user location from database
       boolean connected = getMyLocation();
       myLocation = new LatLng(latitude, longitude); 
       
       if(connected)
       {
        	try {
	            // Loading map
	            initializeMap();
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
        else
        	showToast("Cannot connect to the database."); 
    }
 
    /**
     * function to load map. If map is not created it will create it for you
     * */
    @SuppressLint("NewApi")
	private void initializeMap() {    	
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();            
         //provide radius to draw the circle
          //add a marker
            BitmapDescriptor bitmapDescriptor 
            = BitmapDescriptorFactory.defaultMarker(
              BitmapDescriptorFactory.HUE_AZURE);
            
            googleMap.addMarker(new MarkerOptions()
            .position(myLocation)
            .icon(bitmapDescriptor)
            .title("Your Location"));
            ;
        
         // Move the camera instantly to mylocation with a zoom of 15.
		    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

		    // Zoom in, animating the camera.
		    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);  
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    
    //draw circle on google map
    //this function invokes when the user filters the location
    //when location is filtered, calculate the distance, and mark the points that are in the range
    
    private void drawCircle(int radius){
    	googleMap.clear();
    	try{
    		database = new MySQLDB();
    		connection = database.getConnection();
    		statement = connection.createStatement();
    		
    		String retrieveSql = "SELECT * FROM project.sample_LocationTable;";
    		results = statement.executeQuery(retrieveSql);
    		
    		// Get Location Data from database, calculate the distance, and on GoogleMap with Marker,
    		// If condition is satisfied
    		while(results.next()){
    			latitude = results.getDouble("latitude");
    			longitude = results.getDouble("longitude");
    			double dist = calculateDistance(myLocation.latitude, myLocation.longitude, latitude, longitude, 'K');
    			checkAndMark(dist,radius,latitude,longitude);    			
    		}
    		
    	}catch(SQLException sqle){
    		sqle.printStackTrace();
    		
    	}
    	
    	// Now add Marker
        BitmapDescriptor bitmapDescriptor 
        = BitmapDescriptorFactory.defaultMarker(
          BitmapDescriptorFactory.HUE_AZURE);
        
        googleMap.addMarker(new MarkerOptions()
        .position(myLocation)
        .icon(bitmapDescriptor)
        .title("Your Location"));
        ;
    	// Add a circle 
    	CircleOptions circle=new CircleOptions(); 
    	circle.center(myLocation);
    	circle.strokeColor(0xFFFFA420);
    	circle.strokeWidth(2f);
    	circle.fillColor(0x11FFA420);
    	circle.radius(radius);
    	
    	googleMap.addCircle(circle);
    
    }
    
    // Check the distance and mark if meets the condition, if met - display on Map
    private void checkAndMark(double distance, int radius, double latt, double longt){
    	double rad = radius+0.0;
    	//distance is in kilometers, so lets convert into meters and calculate
    	if(distance * 1000 <= rad){
			//add a marker
			friendLocation = new LatLng(latt,longt); 
	    	googleMap.addMarker(new MarkerOptions()
	        .position(friendLocation)
	        .title("Your Friend"))
	        .showInfoWindow();
		}
    }
    
    
    //calculate the distance between current user and other points with the help of latitudes and longitudes
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2, char unit) {
    	 double theta = lon1 - lon2;
    		  double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    		  dist = Math.acos(dist);
    		  dist = rad2deg(dist);
    		  dist = dist * 60 * 1.609344;
    		  if (unit == 'M') {
    		    dist = dist * 1.1515;
    		  } else if (unit == 'N') {
    		    dist = dist * 0.8684;
    		    }
    		  return (dist);
    }
    
    private double deg2rad(double deg){
    	return (deg * Math.PI / 180.0);
    }
    
    private double rad2deg(double rad) {
    	 return (rad * 180 / Math.PI);
    }
    
    
    //getMyLocation function Definition retrieves my location from database and returns
    public boolean getMyLocation(){
    	latitude = 0.0;
    	longitude = 0.0;
    	
    	try{
    		database = new MySQLDB();
    		connection = database.getConnection();
    		statement = connection.createStatement();
    		
    		String retrieveSql = "SELECT * FROM project.location_Table;";
    		results = statement.executeQuery(retrieveSql);
    		
    		while(results.next()){
    			latitude = results.getDouble("latitude");
    			longitude = results.getDouble("longitude");
    		}
    		return true;
    	}catch(SQLException sqle){
    		sqle.printStackTrace();
    		return false;
    	}
    }
    
    //function definition of populateSampleLocationTable
    @SuppressWarnings("null")
	private void populateSampleLocationTable(){
    	
    	lat = new double[]{
    			53.35280859, 53.35244746, 53.35210218, 53.35164595, 53.35164029, 
    			53.35141161,53.35211576,53.35172859,53.35087116, 53.35021226,
    			53.34988394, 53.34846875, 53.34726863, 53.34712371,
    			53.34447878,53.34445614,53.34445387,53.34560652,53.34418698,53.34668493,
    			53.34671695,53.34767766,53.34873442,53.35386411,53.35169319,53.35007294,53.34861273
		};
    	
    	lon = new double[]{
    			-6.26092225,-6.26107245,-6.26089677,-6.26087531,-6.26063928,-6.26063794,
    			-6.26301706,-6.26350522,-6.26315117,-6.26179933,-6.26063257,-6.25979841,
    			-6.25907957,-6.25850826,-6.25931829,-6.26045555,-6.26285612,-6.26303851,
    			-6.26507967,-6.26021951,-6.26281589,-6.26540154,-6.26701087,-6.26413554,
    			-6.25895351,-6.25572413,-6.25819176
		};
    	
    	//now insert into database
    	//insertData(lat, lon);
    }
    
    /** This function populates the sampleLocationTable for the test purpose */
    private void insertData(double[] lat2, double[] lon2) {
    	try{
    		database = new MySQLDB();
    		connection = database.getConnection();
    		statement = connection.createStatement();
    		
    		int count=0;
    		while(count<lat2.length){
    			double latt = lat2[count];
    			double longt = lon2[count];
    		insertSampleQuery = "insert into sample_LocationTable values('" + latt + "','" + longt + "')";
    		statement.execute(insertSampleQuery);
    		count++;
    		}   		
    		
    	}catch(SQLException sqle){
    		showToast(sqle.getMessage());    		
    	}
		
	}

	@Override
    protected void onResume() {
        super.onResume(); 
        getMyLocation();
        initializeMap();
    }
    
  //method definition to showToast		
  	public void showToast(String message){
  		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  	}


}
