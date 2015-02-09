package com.neverworker.oknow;

import com.parse.ParseUser;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingFragment extends Fragment {
	private static final String OPENVOICE_WEBSITE_URL = "http://openknow.strikingly.com/";
	private MainActivity thisActivity;
	private View rootView;


	public SettingFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_setting, container, false);

		((LinearLayout)rootView.findViewById(R.id.setting_post_amount)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = getResources().getString(R.string.setting_posts_msg);
				Toast.makeText(thisActivity, msg, Toast.LENGTH_SHORT).show();
			}
		});

		((LinearLayout)rootView.findViewById(R.id.setting_care_distance)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = getResources().getString(R.string.setting_distance_msg);
				Toast.makeText(thisActivity, msg, Toast.LENGTH_SHORT).show();
			}
		});

		((LinearLayout)rootView.findViewById(R.id.setting_update)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = getResources().getString(R.string.setting_update_msg);
				Toast.makeText(thisActivity, msg, Toast.LENGTH_SHORT).show();
			}
		});

		((TextView)rootView.findViewById(R.id.setting_about_us)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent targetWebsite = new Intent(Intent.ACTION_VIEW,Uri.parse(OPENVOICE_WEBSITE_URL));
				startActivity(targetWebsite);
			}
		});
		
		TextView logoutButton = (TextView)rootView.findViewById(R.id.setting_logout);
		if (ParseUser.getCurrentUser() != null) {
			logoutButton.setTextColor(Color.BLACK);
			logoutButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ParseUser.logOut();
					thisActivity.getKnowManager().logout();
					String logoutMsg = getResources().getString(R.string.setting_logout_msg);
					Toast.makeText(thisActivity, logoutMsg, Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			logoutButton.setTextColor(Color.LTGRAY);
		}
		
		return rootView;
	}
}
