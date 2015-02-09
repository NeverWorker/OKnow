package com.neverworker.oknow;

import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class KnowFragment extends Fragment {
	private String GOOGLE_SEARCH_URL = "https://www.google.com.tw/search?q=";
	private MainActivity thisActivity;
	private View rootView;
    private GoogleMap map;    
    private TextView knowDistance;
    
    private String tempObjectId;
    private String tempTagName;
    private String tempMessage;
    private ParseGeoPoint tempLocation;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_know, container, false);
		
		if (map == null) {
			map = ((MapFragment) this.getActivity().getFragmentManager().findFragmentById(R.id.know_map)).getMap();
	        map.setMyLocationEnabled(true);
	        map.getUiSettings().setZoomControlsEnabled(false);
	        map.getUiSettings().setMyLocationButtonEnabled(false);
		}
		
		((TextView)rootView.findViewById(R.id.know_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.backFragment();
			}
		});

		((TextView)rootView.findViewById(R.id.know_title)).setText(tempTagName);
		((TextView)rootView.findViewById(R.id.know_message)).setText(tempMessage);
		((TextView)rootView.findViewById(R.id.know_tagname)).setText(tempTagName);
		knowDistance = ((TextView)rootView.findViewById(R.id.know_distance));
		focusLocation(tempLocation);
		
		TextView powerupButton = ((TextView)rootView.findViewById(R.id.know_powerup));
		if (tempObjectId == null) {
			powerupButton.setVisibility(View.GONE);
		} else {
			powerupButton.setVisibility(View.VISIBLE);
			powerupButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("objectId", tempObjectId);
					if (ParseUser.getCurrentUser() != null) {
						String powerupMsg = getResources().getString(R.string.know_powerup_msg);
						final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", powerupMsg, true);
						ParseCloud.callFunctionInBackground("addPopularityOnKnow", params, new FunctionCallback<String>() {
							public void done(String result, ParseException e) {
								dialog.dismiss();
								if (e == null) {
									if (result.equals("1")) {
										String msg = getResources().getString(R.string.know_powerup_success);
										Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
										return;
									}
								}
								String msg = getResources().getString(R.string.know_powerup_failure);
								Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
							}
						});
					} else {
						thisActivity.login();
					}
				}
			});
		}
		
		((TextView)rootView.findViewById(R.id.know_more)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent targetWebsite = new Intent(Intent.ACTION_VIEW,Uri.parse(GOOGLE_SEARCH_URL + tempMessage));
				startActivity(targetWebsite);
			}
		});
		
		return rootView;
	}
	
	public void feedDetail(String objectId, String tagName, String message, ParseGeoPoint location) {
		tempObjectId = objectId;
		tempTagName = tagName;
		tempMessage = message;
		tempLocation = location;
	}
	
	private void focusLocation(ParseGeoPoint location) {
        ParseGeoPoint myLoc = thisActivity.getKnowManager().getLocationInParseGeoPoint();
        if (myLoc == null) {
        	Toast.makeText(thisActivity, getResources().getString(R.string.common_no_locate), Toast.LENGTH_LONG).show();
        	return;
        }
        
		boolean hasTag = false;
        if (location == null) {
			hasTag = false;
			location = myLoc;
		} else {
			hasTag = true;
		}
        
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        if (hasTag) {
			double distance = location.distanceInKilometersTo(myLoc);
			String distanceText;
			if (distance < 1)
				distanceText = getResources().getString(R.string.common_distance_meter, distance*1000);
			else
				distanceText = getResources().getString(R.string.common_distance_kilometer, distance);

			knowDistance.setText(distanceText);
			
			IconGenerator tc = new IconGenerator(thisActivity);
			Bitmap bmp = tc.makeIcon(tempMessage);
			Marker marker = map.addMarker(new MarkerOptions().position(latLng));
			marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));

	        map.moveCamera(CameraUpdateFactory.newCameraPosition(
	        		new CameraPosition.Builder().target(latLng).zoom(17).tilt(60).build()));
	    }
	}
}
