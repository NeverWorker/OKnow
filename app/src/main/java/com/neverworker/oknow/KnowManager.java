package com.neverworker.oknow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.internal.ImageRequest;
import com.facebook.model.GraphUser;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;

public class KnowManager {
	private String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&lang=zh_tw";
	private ArrayList<OnKnowListChangedListener> knowListListeners = new ArrayList<OnKnowListChangedListener>();
	private ArrayList<OnWeatherChangedListener> weatherListeners = new ArrayList<OnWeatherChangedListener>();
	private LocationHelper locHelper;
	
	private ArrayList<ParseObject> knowList = new ArrayList<ParseObject>();
	private HashMap<String, Object> personalData = new HashMap<String, Object>();
	
	private static final int VOICE_UPDATE_INTERVAL = 10000;
	private static final int PERSONAL_UPDATE_INTERVAL = 60000;
	private static final int WEATHER_UPDATE_INTERVAL = 60000;
	private long knowLastUpdate;
	private long personalLastUpdate;
	private long weatherUpdate;
	private boolean knowUpdating;
	private boolean foodUpdating;
	private boolean nearbyUpdating;
	private boolean weatherUpdating;

	private JsonObject weatherData;
	
	public KnowManager(MainActivity mActivity, LocationHelper lh) {
		locHelper = lh;
		
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeFacebookProfile();
		}
	}
	
	public void makeFacebookProfile() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							JSONObject userProfile = new JSONObject();
							try {
								userProfile.put("facebookId", user.getId());
								userProfile.put("name", user.getName());
								if (user.getLocation() != null && user.getLocation().getProperty("name") != null) {
									userProfile.put("location", (String) user
											.getLocation().getProperty("name"));
								}
								if (user.getProperty("gender") != null) {
									userProfile.put("gender",
											(String) user.getProperty("gender"));
								}
								// Save the user profile info in a user property
								ParseUser currentUser = ParseUser
										.getCurrentUser();
								currentUser.put("profile", userProfile);
								currentUser.saveInBackground();
							} catch (JSONException e) {
								e.printStackTrace();
								//android.util.Log.d(IntegratingFacebookTutorialApplication.TAG, "Error parsing returned user data.");
							}
						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								//android.util.Log.d(IntegratingFacebookTutorialApplication.TAG, "The facebook session was invalidated.");
							} else {
								//android.util.Log.d(IntegratingFacebookTutorialApplication.TAG, "Some other error: " + response.getError().getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();
	}
	
	public Location getLocation() {
        return locHelper.getLocation();
	}
	
	public LatLng getLocationInLatLng() {
		Location loc = getLocation();
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        return new LatLng(latitude, longitude);    
	}
	
	public ParseGeoPoint getLocationInParseGeoPoint() {
		Location loc = getLocation();
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        return new ParseGeoPoint(latitude, longitude);
	}
	
	public void updateLocRef() {
		updateKnowList();
		updateWeather();
	}
	
	public void updateDistance() {
		Location loc = getLocation();
		final ParseGeoPoint myLocInParseGeoPoint = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
		Collections.sort(knowList, new Comparator<ParseObject>() {
			public int compare(ParseObject pObj1, ParseObject pObj2) {
				Double dist1 = ((ParseGeoPoint)pObj1.get("location")).distanceInKilometersTo(myLocInParseGeoPoint);
				Double dist2 = ((ParseGeoPoint)pObj2.get("location")).distanceInKilometersTo(myLocInParseGeoPoint);
				return dist1.compareTo(dist2);
			}
		});
	}
	
	private void knowNotification() {
		for (OnKnowListChangedListener listener : knowListListeners) {
			listener.onChanged(knowList);
		}
	}
	
	private void weatherNotification() {
		for (OnWeatherChangedListener listener : weatherListeners) {
			listener.onChanged(weatherData);
		}
	}
	
	public ArrayList<ParseObject> getKnowList() {
		if (knowLastUpdate + VOICE_UPDATE_INTERVAL > System.currentTimeMillis()) 
	        return knowList;
		//updateKnowList();
        return knowList;
	}
		
	public void updateKnowList() {
		if (getLocation() == null)
	        return;
		if (knowUpdating)
			return;
		knowUpdating = true;
		
		HashMap<String, Object> location = new HashMap<String, Object>();
		location.put("location", getLocationInParseGeoPoint());
		ParseCloud.callFunctionInBackground("getKnowByLocation", location, new FunctionCallback<ArrayList<ParseObject>>() {
			public void done(ArrayList<ParseObject> result, ParseException e) {
				if (e == null) {
					knowList.clear();
					for (ParseObject pObj : result) {
						knowList.add(pObj);
					}
					updateDistance();
					knowLastUpdate = System.currentTimeMillis();
				}
				knowNotification();
				knowUpdating = false;
			}
		});
	}

	public void removeKnow(String objectId) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("objectId", objectId);
    	com.parse.ParseCloud.callFunctionInBackground("removeKnow", params, new FunctionCallback<String>() {
			public void done(String result, ParseException e) {
				if (e == null) {
					if (result.equals("1"))
						android.util.Log.d("removeKnow", "success " + result);
					else
						android.util.Log.d("removeKnow", "failure " + result);						
				} else {
					android.util.Log.d("removeKnow", "failure " + e);	        	        					
				}
			}
		});
	}
	
	public HashMap<String, Object> getPersonal() {
		if (personalLastUpdate + PERSONAL_UPDATE_INTERVAL > System.currentTimeMillis())
			return personalData;
		
		try {
			personalData = ParseCloud.callFunction("GetPersonalInfo", new HashMap<String, Object>());
			personalLastUpdate = System.currentTimeMillis();
		} catch (ParseException e) {
			personalData = new HashMap<String, Object>();
		}
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			if (ParseFacebookUtils.isLinked(currentUser)) {
				personalData.put("username", currentUser.getMap("profile").get("name"));
				personalData.remove("personalImage");
				String facebookId = currentUser.getMap("profile").get("facebookId").toString();
				personalData.put("personalImage", getImageFromFacebook(facebookId));
			    
			} else if (ParseTwitterUtils.isLinked(currentUser)) {
				personalData.put("username", "Twitter User");
			} else {
				personalData.put("username", currentUser.getUsername());
			}
			
		}
		
		return personalData;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<ParseObject> getHistory() {
		if (personalData != null && personalData.containsKey("knowList"))
			return (ArrayList<ParseObject>)personalData.get("knowList");
		else
			return null;
	}
	
	public boolean postKnow(String tagName, String message) {
		if (getLocation() == null)
			return false;
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		params.put("tagName", tagName);
		params.put("location", getLocationInParseGeoPoint());
		
		String result;
		try {
			result = ParseCloud.callFunction("createKnow", params);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		
		return result.equals("1");
	}
	
	public void setOnKnowListChangedListener(OnKnowListChangedListener listener) {
		if (knowListListeners.contains(listener) == false)
			knowListListeners.add(listener);
	}
	
	public interface OnKnowListChangedListener extends EventListener {
		void onChanged(ArrayList<ParseObject> list);
	}
	
	private Bitmap getImageFromFacebook(String facebookId) {
	    try {
	        URL url = ImageRequest.getProfilePictureUrl(facebookId, 300, 300).toURL();
	        URLConnection conn = url.openConnection();

	        HttpURLConnection httpConn = (HttpURLConnection)conn;
	        httpConn.setRequestMethod("GET");
	        httpConn.connect();

	        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	            InputStream inputStream = httpConn.getInputStream();

	            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	            inputStream.close();
	            return bitmap;
	        }
	    } catch (MalformedURLException e1) {
	        e1.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (URISyntaxException e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public void logout() {
		knowLastUpdate = 0;
		personalLastUpdate = 0;
	}
	
	public JsonObject getWeather() {
		if (weatherUpdate + WEATHER_UPDATE_INTERVAL > System.currentTimeMillis())
			return weatherData;
		//updateWeather();
		return weatherData;
	}
	public void updateWeather() {
		Location loc = getLocation();
		if (loc == null)
			return;
		if (weatherUpdating)
			return;
		weatherUpdating = true;
		
		new LoadWebTask() {
			protected void onPostExecute(JsonObject result) {
				weatherData = result;
				weatherUpdate = System.currentTimeMillis();
				weatherNotification();
				weatherUpdating = false;
			}
		}.execute(String.format(WEATHER_URL, loc.getLatitude(), loc.getLongitude()));
	}
	
	public void setOnWeatherChangedListener(OnWeatherChangedListener listener) {
		if (weatherListeners.contains(listener) == false)
			weatherListeners.add(listener);
	}
	
	public interface OnWeatherChangedListener  extends EventListener {
		void onChanged(JsonObject array);
	}
	
	private class LoadWebTask extends AsyncTask<String, Void, JsonObject> {
		public String loadHtml(String urlPath) throws IOException {
			StringBuilder result = new StringBuilder();

			URL url = new URL(urlPath);
			HttpURLConnection connection = null;
			connection = (HttpURLConnection)url.openConnection();		
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			
			if (connection.getResponseCode() != 200) {
				return "{error:" + connection.getResponseCode() + "}";
			}
			
			InputStream is;
			BufferedReader rd;
			String line; 

			is = connection.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			while((line = rd.readLine()) != null) {
				result.append(line+"\n");
			}

			rd.close();
			connection.disconnect();

			return result.toString().replace("\u00a0"," ");
		}
		
		@Override
		protected JsonObject doInBackground(String... params) {
			JsonParser parser = new JsonParser();
			try {
				return parser.parse(loadHtml(params[0])).getAsJsonObject();
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	public class Know {
		private ParseObject parseObj;
		public Know(ParseObject pObj) {
			parseObj = pObj;
		}
		
		public ParseGeoPoint getLocation() {
			return parseObj.getParseGeoPoint("location");
		}
		
		public double getDistance(ParseGeoPoint target) {
			return getLocation().distanceInKilometersTo(target);
		}

		public double getDistance(Location target) {
			ParseGeoPoint pTarget = new ParseGeoPoint(target.getLatitude(), target.getLongitude());
			return getLocation().distanceInKilometersTo(pTarget);
		}
	}
}
