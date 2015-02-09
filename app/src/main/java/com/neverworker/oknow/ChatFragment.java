package com.neverworker.oknow;

import java.util.ArrayList;

import com.neverworker.oknow.KnowManager.OnKnowListChangedListener;
import com.neverworker.oknow.KnowManager.OnWeatherChangedListener;
import com.neverworker.oknow.text.WeatherIconMapping;
import com.neverworker.oknow.widget.BlurScrollView;
import com.neverworker.oknow.widget.BlurScrollView.OnScrollChangedListener;

import com.google.gson.JsonObject;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
	private MainActivity thisActivity;
	private LayoutInflater mInflater;

    private View rootView;

	private TextView weatherIcon;
	private TextView weatherText;

    private LinearLayout knowList;

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
		rootView = inflater.inflate(R.layout.fragment_chat, container, false);
		
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
		
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.chat_swipe_container);
		swipeLayout.getBackground().setAlpha(0);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorSchemeResources(android.R.color.white,  
                R.color.oknow_green,
                android.R.color.white, 
                R.color.oknow_green);

		// 設定ScrollView捲動時的事件	1.將上層SwipeLayout的背景Alpha進行改變
		// 							2.滑出前往頂端的按鈕
        BlurScrollView scroller = (BlurScrollView) rootView.findViewById(R.id.chat_scroller);
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
		distanceText = timeStr + ",\n" + distanceText;
		((TextView)itemView.findViewById(R.id.chat_item_distance)).setText(distanceText);

		final String tagName = (String)data.get("tagName");
        TextView tagView = ((TextView)itemView.findViewById(R.id.chat_item_kind));
        tagView.setText(tagName);
        switch (tagName) {
            case "緊急":
            case "警報":
            case "注意":
                tagView.setTextColor(Color.parseColor("#ff0000"));
                break;
            case "交通事故":
                tagView.setTextColor(Color.parseColor("#38cd6c"));
                break;
            case "天氣":
                tagView.setTextColor(Color.parseColor("#00aeef"));
                break;
            case "心情":
                tagView.setTextColor(Color.parseColor("#fff000"));
                break;
            case "美食":
                tagView.setTextColor(Color.parseColor("#f7941d"));
                break;
            case "購物":
                tagView.setTextColor(Color.parseColor("#ff2ca9"));
                break;
            case "玩樂":
                tagView.setTextColor(Color.parseColor("#2dd9c3"));
                break;
            case "住宿":
                tagView.setTextColor(Color.parseColor("#a0e345"));
                break;
        }
		
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
        boolean nextWarningState = false;
		for (ParseObject pObj : list) {
			knowList.addView(makeKnowView(pObj));
            if (!nextWarningState && pObj.get("tagName").equals("警報")) {
                nextWarningState = true;
            }
		}
        toBeWarning = nextWarningState;
        updateBackground();
	}
	
	private void updateWeather(JsonObject jsonObj) {
		JsonObject weatherObj = jsonObj.get("weather").getAsJsonArray().get(0).getAsJsonObject();
		String weatherStr = weatherObj.get("description").getAsString();
		String weatherIconNum = weatherObj.get("icon").getAsString();
		weatherText.setText(weatherStr);
		weatherIcon.setText(WeatherIconMapping.weatherToIcon(weatherIconNum));
	}

    private boolean isWarning = false;
    private boolean toBeWarning = false;
    private void updateBackground() {
        if (isWarning != toBeWarning) {
            if (toBeWarning) {
                rootView.findViewById(R.id.chat_bg_layout).setBackgroundResource(R.drawable.chat_background2);
                rootView.findViewById(R.id.chat_swipe_container).setBackgroundResource(R.drawable.chat_background2_blur);
            } else {
                rootView.findViewById(R.id.chat_bg_layout).setBackgroundResource(R.drawable.chat_background);
                rootView.findViewById(R.id.chat_swipe_container).setBackgroundResource(R.drawable.chat_background_blur);

            }
            isWarning = toBeWarning;
        }
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
