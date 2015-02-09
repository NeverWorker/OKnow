package com.neverworker.oknow;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SimpleListFragment extends Fragment {
	private MainActivity thisActivity;
	private View rootView;
	
	private ArrayList<ParseObject> knowList;
	private KnowAdapter<ParseObject> adapter;
	private ListView listView;
	
	private Resources res;

	public SimpleListFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_simple_list, container, false);
		
		res = getResources();

		((TextView)rootView.findViewById(R.id.simple_list_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.backFragment();
			}
		});
		
		listView = (ListView) rootView.findViewById(R.id.simple_list_container);
		listView.setOnItemClickListener(new OnItemClickListener () {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (adapter != null){
					ParseObject knowObj = (ParseObject) adapter.getItem(position);
					String objectId = knowObj.getObjectId();
					String message = (String)knowObj.get("message");
					ParseGeoPoint location = (ParseGeoPoint) knowObj.get("location");
					String tagName = (String)knowObj.get("tagName");

					thisActivity.switchToKnowFragment(objectId, tagName, message, location);
				}
			}
		});
        
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshKnowList(thisActivity.getKnowManager().getHistory());
	}
	
	public void refreshKnowList(ArrayList<ParseObject> newList) {
		knowList = newList;
		adapter = new KnowAdapter<ParseObject>(getActivity(), R.layout.chat_know_item, 0, knowList);
		listView.setAdapter(adapter);

	}
	
	private class KnowAdapter<T> extends ArrayAdapter<T> {
		private int ourResource;
		private LayoutInflater ourInflator;

		public KnowAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
			super(context, resource, textViewResourceId, objects);
			ourResource = resource;
			ourInflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView;
	        if (convertView == null) {
	        	itemView = ourInflator.inflate(ourResource, parent, false);
	        } else {
	        	itemView = convertView;
	        }
	        
	        ParseObject data = (ParseObject) getItem(position);
	        
			final String message = (String)data.get("message");
			((TextView)itemView.findViewById(R.id.chat_item_know)).setText(message);
			
			final ParseGeoPoint location = (ParseGeoPoint) data.get("location");
			double distance = location.distanceInKilometersTo(thisActivity.getKnowManager().getLocationInParseGeoPoint());
			String distanceText;
			if (distance < 1)
				distanceText = res.getString(R.string.common_distance_meter, distance*1000);
			else
				distanceText = res.getString(R.string.common_distance_kilometer, distance);
			((TextView)itemView.findViewById(R.id.chat_item_distance)).setText(distanceText);

			final String tagName = (String)data.get("tagName");
			((TextView)itemView.findViewById(R.id.chat_item_kind)).setText(tagName);
			
			if (data.containsKey("popularityCount"))
				((TextView)itemView.findViewById(R.id.chat_item_power)).setText("+" + data.get("popularityCount"));
			else
				((TextView)itemView.findViewById(R.id.chat_item_power)).setText("");
			
			((ImageView)itemView.findViewById(R.id.chat_item_arrow)).setColorFilter(Color.parseColor("#AAFFFFFF"));
	        
	        return itemView;
	    }
	}

}
