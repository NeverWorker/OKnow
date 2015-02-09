package com.neverworker.oknow;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.neverworker.oknow.KnowManager.OnKnowListChangedListener;
import com.neverworker.oknow.KnowManager.OnWeatherChangedListener;
import com.neverworker.oknow.common.FileManager;
import com.neverworker.oknow.text.WeatherIconMapping;
import com.neverworker.oknow.widget.BlurScrollView;
import com.neverworker.oknow.widget.BlurScrollView.OnScrollChangedListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
	private MainActivity thisActivity;
	private LayoutInflater mInflater;
	
	private TextView weatherIcon;
	private TextView weatherText;
	
	private RelativeLayout goTopBar;
	private ImageView goTopButton;
	private TextView goTopTitle;
	
	private BlurScrollView scroller;
	private LinearLayout tabKnowBlock;

	private LinearLayout knowList;

	private int previousScroll;
	private Animation slideDownAnim;
	private Animation slideUpAnim;
	
	private OnKnowListChangedListener knowListener;
	private OnWeatherChangedListener weatherListener;
	
	private Resources res;
	
	public ChatFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		
		res = getResources();
		mInflater = inflater;
		View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
		
		weatherIcon = (TextView)rootView.findViewById(R.id.chat_weather_icon);
		weatherText = (TextView)rootView.findViewById(R.id.chat_weather_text);
		Typeface tf = Typeface.createFromAsset(thisActivity.getAssets(), "fonts/Climacons.ttf");
		weatherIcon.setTypeface(tf);
		JsonObject weatherData = thisActivity.getKnowManager().getWeather();
		if (weatherData != null) {
			updateWeather(weatherData);
		} else {
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
		
		goTopBar = ((RelativeLayout)rootView.findViewById(R.id.chat_gotop_bar));
		goTopButton = (ImageView)goTopBar.findViewById(R.id.chat_gotop_button);
		goTopTitle = (TextView)goTopBar.findViewById(R.id.chat_gotop_title);
		
		tabKnowBlock = ((LinearLayout)rootView.findViewById(R.id.chat_tab_know_area));

		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.chat_swipe_container);
		swipeLayout.getBackground().setAlpha(0);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorSchemeResources(android.R.color.white,  
                R.color.oknow_green,
                android.R.color.white, 
                R.color.oknow_green);
		
		slideDownAnim = AnimationUtils.loadAnimation(thisActivity.getApplicationContext(), R.anim.slide_down);
		slideDownAnim.setAnimationListener(new Animation.AnimationListener() {
		    @Override
		    public void onAnimationStart(Animation animation) {
		    	goTopBar.setVisibility(View.VISIBLE);
		    }
		    @Override
		    public void onAnimationEnd(Animation animation) {
		    }
		    @Override
		    public void onAnimationRepeat(Animation animation) {
		    }
		});
		slideUpAnim = AnimationUtils.loadAnimation(thisActivity.getApplicationContext(), R.anim.slide_up);
		slideUpAnim.setAnimationListener(new Animation.AnimationListener() {
		    @Override
		    public void onAnimationStart(Animation animation) {
		    }
		    @Override
		    public void onAnimationEnd(Animation animation) {
		        goTopBar.setVisibility(View.GONE);
		    }
		    @Override
		    public void onAnimationRepeat(Animation animation) {
		    }
		});
		goTopBar.startAnimation(slideUpAnim);
		// 設定ScrollView捲動時的事件	1.將上層SwipeLayout的背景Alpha進行改變
		// 							2.滑出前往頂端的按鈕
		scroller = (BlurScrollView) rootView.findViewById(R.id.chat_scroller);
		scroller.addOnScrollChangedListener(new OnScrollChangedListener() {
			@Override
			public void onChanged(int distance) {
				int blurFactor = distance;
				if (blurFactor < 0)
					blurFactor = 0;
				if (blurFactor > 255)
					blurFactor = 255;
				Drawable background = swipeLayout.getBackground();
				background.setAlpha(blurFactor);
				
				String titleText = getResources().getString(R.string.chat_know_title);
				goTopTitle.setText(titleText);
				
				int popThreshold = (int) tabKnowBlock.getY();
				if ((previousScroll > popThreshold && distance > popThreshold) || (previousScroll < popThreshold && distance < popThreshold))
					return;
				Animation slide;
				if (distance > popThreshold)
					slide = slideDownAnim;
				else
					slide = slideUpAnim;
				goTopBar.startAnimation(slide);
				
				previousScroll = distance;
			}
		});
		
		goTopButton.setColorFilter(Color.WHITE);
		goTopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				scroller.smoothScrollTo(0, 0); 
			}
		});

		knowList = (LinearLayout) rootView.findViewById(R.id.chat_know_list);

		ArrayList<ParseObject> knowList = thisActivity.getKnowManager().getKnowList();
		if (knowList != null && knowList.size() != 0)
			updateKnowList(knowList);
		else {
			if (knowListener == null) {
				knowListener = new OnKnowListChangedListener() {
	    			@Override
	    			public void onChanged(ArrayList<ParseObject> list) {
	    				updateKnowList(list);
	    				knowReady = true;
	    				checkReady();
	    			}
	    		};
			}
    		thisActivity.getKnowManager().setOnKnowListChangedListener(knowListener);
		}

		swipeLayout.setRefreshing(true);
		onRefresh();
		
		return rootView;
	}
	
	private void slowSmoothScroll(final float pos, final int duration, int interval) {
		new android.os.CountDownTimer(duration, interval) { 
	        public void onTick(long millisUntilFinished) { 
	        	scroller.smoothScrollTo(0, (int)((1f-millisUntilFinished/(float)duration)*pos)); 
	        } 
	        public void onFinish() { 
	        	scroller.smoothScrollTo(0, (int)pos); 
	        } 
	     }.start();
	}
	
	private RelativeLayout makeKnowView(ParseObject data) {
		RelativeLayout itemView = (RelativeLayout) mInflater.inflate(R.layout.chat_know_item, knowList, false);
		
		final String objectId = data.getObjectId();
		final String message = (String)data.get("message");
		((TextView)itemView.findViewById(R.id.chat_item_know)).setText(message);
		
		final ParseGeoPoint location = (ParseGeoPoint) data.get("location");
		double distance = location.distanceInKilometersTo(thisActivity.getKnowManager().getLocationInParseGeoPoint());
		String distanceText;
		if (distance < 1)
			distanceText = res.getString(R.string.common_distance_meter, distance*1000);
		else
			distanceText = res.getString(R.string.common_distance_kilometer, distance);
		String timeStr = (String) DateUtils.getRelativeDateTimeString(thisActivity, data.getCreatedAt().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
		distanceText = timeStr + ", " + distanceText;
		((TextView)itemView.findViewById(R.id.chat_item_distance)).setText(distanceText);

		final String tagName = (String)data.get("tagName");
		((TextView)itemView.findViewById(R.id.chat_item_kind)).setText(tagName);
		
		if (data.containsKey("popularityCount"))
			((TextView)itemView.findViewById(R.id.chat_item_power)).setText("+" + data.get("popularityCount"));
		else
			((TextView)itemView.findViewById(R.id.chat_item_power)).setText("");
		
		((ImageView)itemView.findViewById(R.id.chat_item_arrow)).setColorFilter(Color.parseColor("#AAFFFFFF"));
		
		itemView.setOnTouchListener(new View.OnTouchListener() {
			private boolean touchDown;
	        @SuppressLint("ClickableViewAccessibility")
			@Override
	        public boolean onTouch(final View v, MotionEvent event) {
        		touchDown = false;
            	v.setBackgroundColor(Color.TRANSPARENT);
	        	if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        		touchDown = true;
	        		v.postDelayed(new Runnable() {
	        	        @Override
	        	        public void run() {
	        	            if (touchDown)
	        	            	v.setBackgroundColor(Color.parseColor("#44FFFFFF"));
	        	        }
	        	    }, 100);
	        	}
	            return false;
	        }
	    });
		itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.switchToKnowFragment(objectId, tagName, message, location);
			}
		});

		return itemView;
	}
	
	private void updateKnowList(ArrayList<ParseObject> list) {
		knowList.removeAllViews();
		for (ParseObject pObj : list) {
			knowList.addView(makeKnowView(pObj));
		}
	}
	
	private String googleImgPath = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=320&photoreference=%s&sensor=true&key=AIzaSyAw_9t8_7Le9OVbDJfzsC3U3mtZUWTb6js";
	private RelativeLayout makeGooglePlaceAPIView(JsonObject data) {
		RelativeLayout itemView = (RelativeLayout) mInflater.inflate(R.layout.chat_place_item, knowList, false);

		final String name = data.get("name").getAsString();
		((TextView)itemView.findViewById(R.id.chat_item_place_name)).setText(name);

		if (data.has("photos")) {
			JsonArray photoArray = data.get("photos").getAsJsonArray();
			final String placeId = data.get("place_id").getAsString();
			String photosRef = photoArray.get(0).getAsJsonObject().get("photo_reference").getAsString();
			final ImageView pictureView = ((ImageView)itemView.findViewById(R.id.chat_item_picture));
			Bitmap existImage = null;
			if (FileManager.Exist(placeId))
				existImage = FileManager.LoadImage(placeId);
			if ( existImage != null) {
				pictureView.setImageBitmap(existImage);
			} else {
				new FetchImageTask() {
					protected void onPostExecute(Bitmap result) {
						pictureView.setImageBitmap(result);
						FileManager.SaveImage(placeId, result);
					}
				}.execute(String.format(googleImgPath, photosRef));
			}
		}
		
		// XXX: no used while image is cover the background.
		itemView.setOnTouchListener(new View.OnTouchListener() {
			private boolean touchDown;
	        @SuppressLint("ClickableViewAccessibility")
			@Override
	        public boolean onTouch(final View v, MotionEvent event) {
        		touchDown = false;
            	v.setBackgroundColor(Color.TRANSPARENT);
	        	if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        		touchDown = true;
	        		v.postDelayed(new Runnable() {
	        	        @Override
	        	        public void run() {
	        	            if (touchDown)
	        	            	v.setBackgroundColor(Color.parseColor("#44FFFFFF"));
	        	        }
	        	    }, 100);
	        	}
	            return false;
	        }
	    });
		JsonObject geoLoc = data.get("geometry").getAsJsonObject().get("location").getAsJsonObject();
		final ParseGeoPoint location = new ParseGeoPoint(geoLoc.get("lat").getAsDouble(), geoLoc.get("lng").getAsDouble());
		itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.switchToKnowFragment(null, "注意", name, location);
			}
		});
		
		return itemView;
	}
	
	private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				URL url = new URL(params[0]);
				Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				return bmp;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private void updateWeather(JsonObject jsonObj) {
		JsonObject weatherObj = jsonObj.get("weather").getAsJsonArray().get(0).getAsJsonObject();
		String weatherStr = weatherObj.get("description").getAsString();
		String weatherIconNum = weatherObj.get("icon").getAsString();
		weatherText.setText(weatherStr);
		weatherIcon.setText(WeatherIconMapping.weatherToIcon(weatherIconNum));
	}
	
	private SwipeRefreshLayout swipeLayout;
	private boolean isRefresh = false; //是否刷新中 
	private boolean knowReady = false;
	@Override
	public void onRefresh() {
		if(!isRefresh) { 
			isRefresh = true;  
		    new Handler().postDelayed(new Runnable() {  
		        public void run() {  
		            knowReady = false;

		            thisActivity.getKnowManager().updateLocRef();
		        }  
		    }, 3000);
	    }
	}  
    private void checkReady() {
    	if (knowReady) {
            isRefresh = false;
            swipeLayout.setRefreshing(false);
    	}
    }
}
