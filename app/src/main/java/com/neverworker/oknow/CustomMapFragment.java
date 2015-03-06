package com.neverworker.oknow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonParser;
import com.neverworker.oknow.KnowManager.OnKnowListChangedListener;
import com.neverworker.oknow.KnowManager.OnWeatherChangedListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.maps.android.ui.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.neverworker.oknow.common.FileManager;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomMapFragment extends Fragment {
	private MainActivity thisActivity;
	private View rootView;
    private GoogleMap map;
    private TextView weatherView;
    private TextView temperatureView;
    
    private HashMap<Marker, ParseObject> markerKnowMap = new HashMap<Marker, ParseObject>();
    private HashMap<Marker, JsonObject> markerPlaceMap = new HashMap<Marker, JsonObject>();

	private OnKnowListChangedListener knowListener;
	private OnWeatherChangedListener weatherListener;

    private static JsonArray refugeeData;
    private ArrayList<JsonObject> nearRefugeeData = new ArrayList<JsonObject>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_map, container, false);

        if (map == null) {
			map = ((MapFragment) this.getActivity().getFragmentManager().findFragmentById(R.id.google_map)).getMap();
	        map.setMyLocationEnabled(true);
	        map.getUiSettings().setZoomControlsEnabled(false);
	        map.getUiSettings().setMyLocationButtonEnabled(false);

	        focusMyLocation();
        }
		
        TextView buttonWhere = (TextView) rootView.findViewById(R.id.map_where_am_i);
        buttonWhere.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				focusMyLocation();
			}
        });
        
        weatherView = (TextView) rootView.findViewById(R.id.map_weather);
        temperatureView = (TextView) rootView.findViewById(R.id.map_temperature);
        
        ImageView knowButton = (ImageView) rootView.findViewById(R.id.map_know_icon);
        knowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<ParseObject> list = thisActivity.getKnowManager().getKnowList();
				updateKnowList(list);
			}
        });
        
        map.setOnMarkerClickListener(new OnMarkerClickListener() {
        	Marker previousMarker = null;
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.equals(previousMarker)) {
					if (markerKnowMap.containsKey(marker)) {
						ParseObject pObj = markerKnowMap.get(marker);
						thisActivity.switchToKnowFragment(pObj.getObjectId(), (String)pObj.get("tagName"), (String)pObj.get("message"), (ParseGeoPoint)pObj.get("location"));						
					} else if (markerPlaceMap.containsKey(marker)) {
						JsonObject jObj = markerPlaceMap.get(marker);
						JsonObject geoLoc = jObj.get("geometry").getAsJsonObject().get("location").getAsJsonObject();
						final ParseGeoPoint location = new ParseGeoPoint(geoLoc.get("lat").getAsDouble(), geoLoc.get("lng").getAsDouble());
						thisActivity.switchToKnowFragment(null, "注意", jObj.get("name").getAsString(), location);
					}
				}
				else {
					map.animateCamera(CameraUpdateFactory.newCameraPosition(
			        		new CameraPosition.Builder().target(marker.getPosition()).zoom(16).tilt(45).build()));
				}
				previousMarker = marker;
				return true;
			}
        });
        
		ArrayList<ParseObject> knowList = thisActivity.getKnowManager().getKnowList();
		if (knowList != null)
			updateKnowList(knowList);
		else {
			if (knowListener == null) {
				knowListener = new OnKnowListChangedListener() {
					@Override
					public void onChanged(ArrayList<ParseObject> list) {
						updateKnowList(list);
					}
					
				};
			}
			thisActivity.getKnowManager().setOnKnowListChangedListener(knowListener);
		}
		
		JsonObject weatherData = thisActivity.getKnowManager().getWeather();
		if (weatherData != null) 
			updateWeather(weatherData);
		else {
			if (weatherListener == null) {
				weatherListener = new OnWeatherChangedListener() {
					@Override
					public void onChanged(JsonObject array) {
						if (array != null) 
							updateWeather(array);				
					}
					
				};
			}
			thisActivity.getKnowManager().setOnWeatherChangedListener(weatherListener);
		}

        if (refugeeData == null) {
            Location mLoc = thisActivity.getKnowManager().getLocation();
            if (mLoc != null) {
                try {
                    InputStream is = thisActivity.getAssets().open("embedData/refugee.json");
                    refugeeData = new JsonParser().parse(FileManager.ReadInputStreamToStr(is, "UTF-8")).getAsJsonArray();
                    nearRefugeeData.clear();
                    for (JsonElement oriJEle : refugeeData) {
                        JsonObject newJObj = oriJEle.getAsJsonObject();
                        if (!newJObj.has("location"))
                            continue;
                        if (nearRefugeeData.size() < 10) {
                            nearRefugeeData.add(newJObj);
                        } else {
                            int fixIndex = -1;
                            for (JsonObject oriJObj : nearRefugeeData) {
                                if (KnowManager.compareLocLatLngL(thisActivity.getKnowManager().getLocation(),
                                        newJObj.get("location").getAsJsonObject(),
                                        oriJObj.get("location").getAsJsonObject())) {
                                    fixIndex = nearRefugeeData.indexOf(oriJObj);
                                    break;
                                }
                            }
                            if (fixIndex != -1) {
                                nearRefugeeData.remove(fixIndex);
                                nearRefugeeData.add(fixIndex, newJObj);
                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        updateRefugeeList(nearRefugeeData);

		return rootView;
	}
	
	private void focusMyLocation() {
        Location loc = thisActivity.getKnowManager().getLocation();
        if (loc == null) {
        	Toast.makeText(thisActivity, getResources().getString(R.string.common_no_locate), Toast.LENGTH_LONG).show();
        	return;
        }
        
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        map.animateCamera(CameraUpdateFactory.newCameraPosition(
        		new CameraPosition.Builder().target(latLng).zoom(16).tilt(45).build()));
	}
	
	private Marker addMarker(ParseObject pObj) {
		ParseGeoPoint parseLocation = (ParseGeoPoint)pObj.get("location");
		LatLng location = new LatLng(parseLocation.getLatitude(), parseLocation.getLongitude());
		
		String title = (String)pObj.get("message");
		String iconText = title.split("\n")[0];
		if (iconText.length() > 12) 
			iconText = iconText.substring(0, 11) + "...";
		IconGenerator tc = new IconGenerator(thisActivity);
		Bitmap bmp = tc.makeIcon(iconText);
		Marker marker = map.addMarker(new MarkerOptions().position(location).title(title));
		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
		
		return marker;
	}
	
	private Marker addMarker(JsonObject jObj) {
		JsonObject geoLoc = jObj.get("geometry").getAsJsonObject().get("location").getAsJsonObject();
		LatLng location = new LatLng(geoLoc.get("lat").getAsDouble(), geoLoc.get("lng").getAsDouble());
		
		String title = jObj.get("name").getAsString();
		String iconText = title.split("\n")[0];
		if (iconText.length() > 12) 
			iconText = iconText.substring(0, 11) + "...";
		IconGenerator tc = new IconGenerator(thisActivity);
		Bitmap bmp = tc.makeIcon(iconText);
		Marker marker = map.addMarker(new MarkerOptions().position(location).title(title));
		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
		
		return marker;
	}
	
	/* 
	 * 由取得的陣列來更新坐標資訊
	 */
	private void updateKnowList(ArrayList<ParseObject> list) {
		map.clear();
		markerKnowMap.clear();
		for (ParseObject pObj : list) {
			Marker marker = addMarker(pObj);
			markerKnowMap.put(marker, pObj);
		}
	}
	
	private void updateRefugeeList(ArrayList<JsonObject> list) {
		//map.clear();
		//markerPlaceMap.clear();
		for (JsonObject jObj : list) {
			Marker marker = addRefugeeMarker(jObj);
            //if (marker != null) {
    		//	markerPlaceMap.put(marker, jObj);
		}
	}
	
	private void updateWeather(JsonObject jsonObj) {
		float temperature = jsonObj.get("main").getAsJsonObject().get("temp").getAsFloat();
		temperatureView.setText(getResources().getString(R.string.common_temperature, (temperature-273.15)));
		String weatherStr = jsonObj.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
		weatherView.setText(weatherStr);
	}


    private Marker addRefugeeMarker(JsonObject jObj) {
        if (!jObj.has("location"))
            return null;
        JsonObject geoLoc = jObj.get("location").getAsJsonObject();
        if (!geoLoc.has("latitude") || !geoLoc.has("longitude"))
            return null;
        LatLng location = new LatLng(geoLoc.get("latitude").getAsDouble(), geoLoc.get("longitude").getAsDouble());

        String title = jObj.get("name").getAsString() + "(避難所)";
        String iconText = title.split("\n")[0];
        if (iconText.length() > 12)
            iconText = iconText.substring(0, 11) + "...";
        IconGenerator tc = new IconGenerator(thisActivity);
        tc.setTextAppearance(R.style.iconGenRefugeeText);
//        tc.setColor(Color.parseColor("#633300"));
        Bitmap bmp = tc.makeIcon(iconText);
        Marker marker = map.addMarker(new MarkerOptions().position(location).title(title));
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));

        return marker;
    }

}
