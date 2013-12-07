package nl.rhoek.mygpspos;

import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
 
public class GPSTracker extends Service implements LocationListener, GpsStatus.Listener {
 
    private final Context mContext;
 
    // GPS status object
    private GpsStatus status = null;
  
    private boolean hasLocationProviders = false;

    private Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude
 
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
 
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second
 
    // Declaring a Location Manager
    private LocationManager locationManager;
 
    private void initLocationManager(){
    	if(locationManager == null){
	        try {
	        	// get location manager service
	            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
	            
	            // Listen for GPS status update
	            locationManager.addGpsStatusListener(this);
	            
	            // start GPS listeners
	            startUsingGPS();
	            
	        } catch (Exception e) {
	        	locationManager = null;
	            e.printStackTrace();
	        }
    	}
    }
    
    private void initLocation(){
        List<String> providers = locationManager.getProviders(true);
        for(int i=0; i < providers.size();  i++) {
        	String provider = providers.get(i);
            // Get last know location of the provider
            location = locationManager.getLastKnownLocation(provider); 
        }
    }
    
    public GPSTracker(Context context) {
        this.mContext = context;
    	initLocationManager();
    }
 
    /**
     * Start using GPS listener
     */
    
    protected void startUsingGPS(){
    	if(locationManager != null){
    		status = locationManager.getGpsStatus(status);
    		hasLocationProviders = false;
	        // Register all available provider (no matter whether they are not enabled !)
	        List<String> providers = locationManager.getAllProviders();
	        for(int i=0; i < providers.size();  i++) {
	        	String provider = providers.get(i);
	        	Log.d("GPS Tracker", "Added provider: " + provider);
	        	
	        	// Reqister for location updates based on provider
	            locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	            // Get last know location of the provider
	            location = locationManager.getLastKnownLocation(provider);
	            if (location != null) {
	                latitude = location.getLatitude();
	                longitude = location.getLongitude();
	            }
	            
	            // GPS can be requested, but need to check is the providers are enabled!
	            // - ignore passive provider in this check 
	        	if(provider.compareToIgnoreCase(LocationManager.PASSIVE_PROVIDER) != 0){
	        		hasLocationProviders = true;
	        	}            
	            if(locationManager.isProviderEnabled(provider)) {
	            	onProviderEnabled(provider);
	            }
	        }    	
    	}
    }
    
    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    protected void stopUsingGPS(){
    	if(locationManager != null){
    		hasLocationProviders = false;
    		locationManager.removeUpdates(GPSTracker.this);
    	}
    }
     
    /**
     * Get reference to GPS status object
     */
    public GpsStatus getStatus() {
    	return status;
    }
    
    /**
     * Get reference to location object
     */
    public Location getLocation() {
    	initLocation();
        return location;
    }
    
    /**
     * Function to get latitude
     * */
    public double getLatitude(){
    	initLocation();
        if(location != null){
            latitude = location.getLatitude();
        }
         
        // return latitude
        return latitude;
    }
     
    /**
     * Function to get longitude
     * */
    public double getLongitude(){
    	initLocation();
        if(location != null){
            longitude = location.getLongitude();
        }
         
        // return longitude
        return longitude;
    }
     
    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() { 
    	boolean ok = false;
    	if(hasLocationProviders){
        	// are there providers enabled?
    		List<String> providers = locationManager.getProviders(true);
    		for(int i = 0; i < providers.size(); i++){
    			String provider = providers.get(i);
    			if(provider.compareToIgnoreCase(LocationManager.PASSIVE_PROVIDER) != 0){
    				ok = true;
    			}
    		}
    	}
    	return ok;
    }
     
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will launch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * Service 'onBind' override
     */
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Events of LocationListener
     */
 
    @Override
    public void onLocationChanged(Location location) {
    	this.location = location;
    	
    	// send update
    	GPSPosActivity act = (GPSPosActivity) this.mContext;
    	act.ShowGPSLocation(location);
    }
 
    @Override
    public void onProviderDisabled(String provider) {
    	Log.d("GPS Tracker", "Provider disabled: " + provider);
    }
 
    @Override
    public void onProviderEnabled(String provider) {
    	Log.d("GPS Tracker", "Provider enabled: " + provider);
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	Log.d("GPS Tracker", String.format("Provider status: %s - %d", provider, status));
    }
 
    /**
     * Events of GPSStatus.Lister
     */
    
	@Override
	public void onGpsStatusChanged(int event) {
		status = locationManager.getGpsStatus(status);	
	}
}