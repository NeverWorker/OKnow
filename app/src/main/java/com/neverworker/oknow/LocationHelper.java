package com.neverworker.oknow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationHelper implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 5000;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = 1000;
    // Update distance in meters
    private static final long DISTANCE_INTERVAL = 100;
    
	private SharedPreferences mPrefs;
	private Editor mEditor;
	
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private boolean mUpdatesRequested;
	
	private Location newestLoc;
	
	public LocationHelper(Activity activity) {
		// Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        // Set the distance update interval to 100 meters
        mLocationRequest.setSmallestDisplacement(DISTANCE_INTERVAL);
        
        mPrefs = activity.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        
		mLocationClient = new LocationClient(activity, this, this);
		mUpdatesRequested = true;
	}
	
	protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
    }
	
	protected void onStart() {
        mLocationClient.connect();
    }
	
	protected void onResume() {
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
    }
	
	protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
        	mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
    }
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		android.util.Log.d("LocationHelper", "ConnectionFailed!!");
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// If already requested, start periodic updates
		android.util.Log.d("LocationHelper", "Connected");
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
	}

	@Override
	public void onDisconnected() {
		android.util.Log.d("LocationHelper", "Disconnected");
	}

	@Override
	public void onLocationChanged(Location arg0) {
		android.util.Log.d("LocationHelper", "LocationChanged");
		newestLoc = arg0;
	}
	
	public Location getLocation() {
		return newestLoc;
	}

}
